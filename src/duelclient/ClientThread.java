package duelclient;

import java.io.IOException;
import java.io.InputStream;

public class ClientThread implements Runnable
{
    private Thread _t;
    private InputStream _in;
    private String _name;
    private long _guid;

    ClientThread()
    {
        try
        {
            _in = Client.getInstance().getSocket().getInputStream();
        }
        catch (IOException e){ }

        _t = new Thread(this);
        _t.start();
    }
    
    public long getGuid()
    {
        return _guid;
    }

    public void run()
    {
        boolean loop = true;
        try
        {
            while (loop)
            {
                byte[] dataBytes = new byte[8];
                int data = _in.read(dataBytes, 0, 8);
                if (data == -1)
                    loop = false;
                else
                {
                    Packet firstPacket = new Packet(dataBytes);
                    int len = firstPacket.getInt();
                    int opcode = firstPacket.getInt();
                    byte[] messageArray = new byte[len];
                    if (len > 0)
                    {
                        int result = _in.read(messageArray, 0, len);
                        if (result == -1)
                            loop = false;
                        else
                        {
                            Packet messagePacket = new Packet(messageArray);
                            switch (opcode)
                            {
                                case 0:
                                    handleAcceptConnect(messagePacket);
                                    break;
                                case 1:
                                    handleReceiveWorldPlayers(messagePacket);
                	            break;
                	        case 2:
                                    handleReceiveGames(messagePacket);
                	            break;
                                case 3:
                                    handleAddGame(messagePacket);
                                    break;
                                case 4:
                                    handleCreateGame(messagePacket);
                                    break;
                                case 5:
                                    handleGameInfo(messagePacket);
                                    break;
                                case 6:
                                    handleGamersInfo(messagePacket);
                                    break;
                                case 7:
                                    handleJoinGame(messagePacket);
                                    break;
                                case 8:
                                    handleReceiveLogSystem(messagePacket);
                                    break;
                                case 9:
                                    handleReceiveChat(messagePacket);
                                    break;
                                case 10:
                                    handleChangeState(messagePacket);
                                    break;
                                case 11:
                                    handleChangeType(messagePacket);
                                    break;
                                case 12:
                                    handleLoadStartGame(messagePacket);
                                    break;
                                case 13:
                                    handleShowGame(messagePacket);
                                    break;
                                case 14:
                                    handleForceLeavePartie(messagePacket);
                                    break;
                                case 15:
                                    handleStartGame(messagePacket);
                                    break;
                                case 16:
                                    handleUpdateDelta(messagePacket);
                                    break;
                                case 17:
                                    handleUnitFire(messagePacket);
                                    break;
                                case 18:
                                    handleAddMob(messagePacket);
                                    break;
                                case 19:
                                    handleDestroyObject(messagePacket);
                                    break;
                                case 20:
                                    handleUpdatePosition(messagePacket);
                                    break;
                                case 21:
                                    handleUpdateScore(messagePacket);
                                    break;
                                case 22:
                                    handleAddPlayerShip(messagePacket);
                                    break;
                            }
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            System.out.println(e.toString());
        }
        finally // finally se produira le plus souvent lors de la deconnexion du client
        {
            try
            {
                Client.getInstance().disconnect();
            	Client.getInstance().getSocket().close();
            }
            catch (IOException e){ }
        }
    }
    
    private void handleAcceptConnect(Packet messagePacket)
    {
        long guid = messagePacket.getLong();
        String name = messagePacket.getString();
        Client.getInstance().onConnected();
        Client.getInstance().sendName(name);
        _name = name;
        _guid = guid;
    }
    
    private void handleReceiveWorldPlayers(Packet messagePacket)
    {
        long nbPlayers = messagePacket.getLong();
        Client.getInstance().sendNbPlayers(nbPlayers);
    }
    
    private void handleReceiveGames(Packet messagePacket)
    {
        long nbGames = messagePacket.getLong();
        Client.getInstance().sendNbGames(nbGames);
    }
    
    private void handleAddGame(Packet messagePacket)
    {
        byte type = messagePacket.getByte();
        int players = messagePacket.getInt();
        byte state = messagePacket.getByte();
        String name = messagePacket.getString();
        DuelGame server = new DuelGame(name, type, players, state);
        Client.getInstance().addGame(server);
    }
    
    private void handleCreateGame(Packet messagePacket)
    {
        byte result = messagePacket.getByte();
        switch (result)
        {
            case 1:
                Client.getInstance().errorCreateOccuped();
                break;
            case 2:
                Client.getInstance().errorExist();
                break;
            case 3:
                Client.getInstance().enterGame();
                break;
        }
    }
    
    private void handleGameInfo(Packet messagePacket)
    {
        byte type = messagePacket.getByte();
        Client.getInstance().sendType(type);
        
        int players = messagePacket.getInt();
        Client.getInstance().sendNbGamers(players);

        String name = messagePacket.getString();
        Client.getInstance().sendNameGame(name);
    }
    
    private void handleGamersInfo(Packet messagePacket)
    {
        long guid = messagePacket.getLong();
        byte Class = messagePacket.getByte();
        byte Ready = messagePacket.getByte();
        String name = messagePacket.getString();

        DuelPlayer player = new DuelPlayer(guid, name, Class, Ready);
        Client.getInstance().addPlayer(player);
    }
    
    private void handleJoinGame(Packet messagePacket)
    {
        byte result = messagePacket.getByte();
        switch (result)
        {
            case 1:
                Client.getInstance().errorOccuped();
                return;
            case 3:
                Client.getInstance().errorMany();
                return;
            case 4:
                Client.getInstance().errorState();
                return;
            case 5:
                Client.getInstance().enterGame();
        }
    }
    
    private void handleReceiveLogSystem(Packet messagePacket)
    {
        byte result = messagePacket.getByte();
        String msg = messagePacket.getString();
        Client.getInstance().sendLog(result, msg);
    }
    
    private void handleReceiveChat(Packet messagePacket)
    {
        long guid = messagePacket.getLong();
        DuelPlayer player = Client.getInstance().getDuelPlayer(guid);
        if (player != null)
        {
            String name = player.getName();
            String text = messagePacket.getString();
            String textReplace = text.replaceAll("-", " ");
           Client.getInstance().sendLog((byte) 0, "<" + name + "> " + textReplace);
        }
    }
    
    private void handleChangeState(Packet messagePacket)
    {
        long guid = messagePacket.getLong();
        Client.getInstance().setReady(guid);
    }
    
    private void handleChangeType(Packet messagePacket)
    {
        byte type = messagePacket.getByte();
        long guid = messagePacket.getLong();
        Client.getInstance().setClass(guid, type);
    }
    
    private void handleLoadStartGame(Packet messagePacket)
    {
        Client.getInstance().loadStart();
    }
    
    private void handleShowGame(Packet messagePacket)
    {
        Client.getInstance().showGame();
    }
    
    private void handleForceLeavePartie(Packet messagePacket)
    {
        Client.getInstance().leavePartie();
    }
    
    private void handleStartGame(Packet messagePacket)
    {
        Client.getInstance().startGame(messagePacket);
    }
    
    private void handleUpdateDelta(Packet messagePacket)
    {
        Client.getInstance().updateDelta(messagePacket);
    }
    
    private void handleUnitFire(Packet messagePacket)
    {
        Client.getInstance().receiveFire(messagePacket);
    }
    
    private void handleAddMob(Packet messagePacket)
    {
        Client.getInstance().addMob(messagePacket);
    }
    
    private void handleDestroyObject(Packet messagePacket)
    {
        Client.getInstance().destroyObject(messagePacket);
    }
    
    private void handleUpdatePosition(Packet messagePacket)
    {
        Client.getInstance().updatePosition(messagePacket);
    }
    
    private void handleUpdateScore(Packet messagePacket)
    {
        Client.getInstance().updateScore(messagePacket);
    }
    
    private void handleAddPlayerShip(Packet messagePacket)
    {
        Client.getInstance().addPlayerShip(getGuid(), messagePacket);
    }
}