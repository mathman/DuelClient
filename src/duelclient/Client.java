package duelclient;

import java.io.IOException;
import java.net.Socket;

public class Client
{
    private static Client instance = null;
    private ClientThread _thread;
    private GamesWindow _GamesWindow;
    private ConnexionWindow _ConnexionWindow;
    private NewGameWindow _NewGameWindow;
    private GameWindow _GameWindow;
    private PartieWindow _PartieWindow;
    private ErrorWindow _errorWindow;
    private Socket _socket;
    public int _WindowState;
    
    public static final int CONNEXION = 0;
    public static final int GAMES = 1;
    public static final int NEWGAME = 2;
    public static final int GAME = 3;
    public static final int PARTIE = 4;
    public static final int ERROR = 999;
    
    private Client()
    {
        _socket = null;
        _thread = null;
        _GamesWindow = null;
        _NewGameWindow = null;
        _GameWindow = null;
        _PartieWindow = null;
        _errorWindow = null;
        _ConnexionWindow = new ConnexionWindow();
    }
    
    public static Client getInstance()
    {
        if (instance == null)
            instance = new Client();
           
        return instance;
    }

    public Socket getSocket()
    {
    	return _socket;
    }
    
    public ClientThread getClientThread()
    {
        return _thread;
    }
    
    public DuelPlayer getDuelPlayer(long guid)
    {
        return _GameWindow.getDuelPlayer(guid);
    }

    public void connexion(String name, String host)
    {
        try
        {
            _socket = new Socket(host, 20000);
            _thread = new ClientThread();
            Packet packet = new Packet(0, 4 + name.toCharArray().length*2);
            packet.putString(name);
            sendPacket(packet);
	}
        catch (Exception e)
        {
            _WindowState = ERROR;
            _errorWindow = new ErrorWindow(_ConnexionWindow.getLocation());
            _ConnexionWindow.dispose();
            _ConnexionWindow = null;
        }
    }
    
    public void sendPacket(Packet packet)
    {
        try
        {
            _socket.getOutputStream().write(packet.getByteArray());
        }
        catch (IOException e){ }
    }

    public void onConnected()
    {
        _WindowState = GAMES;
        _GamesWindow = new GamesWindow(_ConnexionWindow.getLocation());
    	_ConnexionWindow.dispose();
        _ConnexionWindow = null;
        refreshGames();
    }
    
    public void sendName(String name)
    {
        _GamesWindow.setNamePlayer(name);
    }

    public void sendNbPlayers(long nbPlayers)
    {
    	_GamesWindow.setNbPlayers(nbPlayers);
    }

    public void sendNbGames(long nbGames)
    {
        _GamesWindow.removeAllServers();
    	_GamesWindow.setNbGames(nbGames);
    }
    
    public void addGame(DuelGame server)
    {
    	_GamesWindow.addGame(server);
    }
    
    public void sendNbGamers(int nbGamers)
    {
        _GameWindow.removeAllPlayers();
    	_GameWindow.setNbPlayers(nbGamers);
    }
    
    public void sendType(byte type)
    {
        String typeText = "";
        switch (type)
        {
            case 0:
                typeText = "JCE";
                break;
            case 1:
                typeText = "JCJ";
                break;
        }
        
        _GameWindow.setType(typeText);
    }
    
    public void addPlayer(DuelPlayer player)
    {
    	_GameWindow.addGamer(player);
    }
    
    public void sendNameGame(String name)
    {
        _GameWindow.setName(name);
    }

    public void sendDisconnect()
    {
        Packet packet = new Packet(6, 1);
        packet.putByte((byte) 0);
        sendPacket(packet);
    	try
        {
    	    _socket.close();
        }
    	catch (IOException e){ }
        disconnect();
    }
    
    public void disconnect()
    {
        _WindowState = CONNEXION;
        _ConnexionWindow = new ConnexionWindow(_GamesWindow.getLocation());
        if (_GamesWindow != null)
        {
    	    _GamesWindow.dispose();
            _GamesWindow = null;
        }
        if (_GameWindow != null)
        {
            _GameWindow.dispose();
            _GameWindow = null;
        }
        if (_PartieWindow != null)
        {
            Partie.getInstance().killThread();
            _PartieWindow.dispose();
            _PartieWindow = null;
        }
        if (_NewGameWindow != null)
        {
            _NewGameWindow.dispose();
            _NewGameWindow = null;
        }
    }
    
    public void refreshGames()
    {
        _GamesWindow.hideError();
        Packet packet = new Packet(1, 1);
        packet.putByte((byte) 0);
        sendPacket(packet);
    }
    
    public void createGame()
    {
        _WindowState = NEWGAME;
        _GamesWindow.hideError();
        _GamesWindow.setVisible(false);
        _NewGameWindow = new NewGameWindow(_GamesWindow.getLocation());
    }
    
    public void removeCreateGame()
    {
        _WindowState = GAMES;
        _GamesWindow.setVisible(true);
        _NewGameWindow.dispose();
        _NewGameWindow = null;
    }
    
    public void errorCreateOccuped()
    {
        _NewGameWindow.errorCreateOccuped();
    }
    
    public void errorExist()
    {
        _NewGameWindow.errorExist();
    }
    
    public void enterGame()
    {
        switch (_WindowState)
        {
            case GAMES:
                _GamesWindow.hideError();
                _GameWindow = new GameWindow(_GamesWindow.getLocation());
                break;
            case NEWGAME:
                _GameWindow = new GameWindow(_NewGameWindow.getLocation());
                _NewGameWindow.dispose();
                _NewGameWindow = null;
                break;
        }
        _GamesWindow.setVisible(false);
        Packet packet = new Packet(4, 1);
        packet.putByte((byte) 0);
        sendPacket(packet);
        _WindowState = GAME;
    }
    
    public void leaveGame()
    {
        Packet packet = new Packet(3, 1);
        packet.putByte((byte) 0);
        sendPacket(packet);
        _GamesWindow.setVisible(true);
        refreshGames();
        switch (_WindowState)
        {
            case GAME:
                _GameWindow.dispose();
                _GameWindow = null;
                break;
            case PARTIE:
                Partie.getInstance().killThread();
                _PartieWindow.dispose();
                _PartieWindow = null;
                break;
        }
        _WindowState = GAMES;
    }
    
    public void leavePartie()
    {
        _GameWindow.resetTimer();
        _GameWindow.setVisible(true);
        Packet packet = new Packet(10, 1);
        packet.putByte((byte) 0);
        sendPacket(packet);
        if (_WindowState == PARTIE)
        {
            Partie.getInstance().killThread();
            _PartieWindow.dispose();
            _PartieWindow = null;
        }
        _WindowState = GAME;
    }
    
    public void backConnexion()
    {
        _ConnexionWindow = new ConnexionWindow(_errorWindow.getLocation());
        _errorWindow.dispose();
        _errorWindow = null;
        _WindowState = CONNEXION;
    }
    
    public void joinServeur(String namegame)
    {
        Packet packet = new Packet(5, 4 + namegame.toCharArray().length*2);
        packet.putString(namegame);
        sendPacket(packet);
    }
    
    public void errorOccuped()
    {
        _GamesWindow.errorOccuped();
    }
    
    public void errorMany()
    {
        _GamesWindow.errorMany();
    }
    
    public void errorState()
    {
        _GamesWindow.errorState();
    }
    
    public void sendLog(byte result, String log)
    {
        if (result == 0)
        {
            switch (_WindowState)
            {
                case GAME:
                    _GameWindow.sendLog(log);
                    break;
                case PARTIE:
                    _PartieWindow.sendLog(log);
                    break;
            }
        }
        else if (result == 1)
        {
            if (_GameWindow != null)
                _GameWindow.sendLog(log);
        }
        else if (result == 2)
        {
            if (_PartieWindow != null)
                _PartieWindow.sendLog(log);
        }
    }
    
    public void sendRequestReady()
    {
        Packet packet = new Packet(8, 1);
        packet.putByte((byte) 0);
        sendPacket(packet);
    }
    
    public void sendRequestChangeClass(byte Class)
    {
        Packet packet = new Packet(9, 4);
        packet.putByte(Class);
        sendPacket(packet);
    }
    
    public void setReady(long guid)
    {
        _GameWindow.setReady(guid);
    }
    
    public void setClass(long guid, byte Class)
    {
        _GameWindow.setClass(guid, Class);
    }
    
    public void loadStart()
    {
        if (_WindowState == GAME)
        {
            _GameWindow.loadStart();
        }
    }
    
    public void showGame()
    {
        _WindowState = PARTIE;
        _PartieWindow = new PartieWindow();
        _GameWindow.resetTimer();
        _GameWindow.setVisible(false);
        Packet packet = new Packet(12, 1);
        packet.putByte((byte) 0);
        sendPacket(packet);
    }
    
    public void startGame(Packet messagePacket)
    {
        _PartieWindow.startGame(messagePacket);
    }
    
    public void updateDelta(Packet messagePacket)
    {
        _PartieWindow.updateDelta(messagePacket);
    }
    
    public void updatePosition(Packet messagePacket)
    {
        _PartieWindow.updatePosition(messagePacket);
    }
    
    public void updateScore(Packet messagePacket)
    {
        _PartieWindow.updateScore(messagePacket);
    }
    
    public void receiveFire(Packet messagePacket)
    {
        _PartieWindow.receiveFire(messagePacket);
    }
    
    public void addMob(Packet messagePacket)
    {
        _PartieWindow.addMob(messagePacket);
    }
    
    public void addPlayerShip(long guid, Packet messagePacket)
    {
        _PartieWindow.addPlayerShip(guid, messagePacket);
    }
    
    public void destroyObject(Packet messagePacket)
    {
        _PartieWindow.destroyObject(messagePacket);
    }
}