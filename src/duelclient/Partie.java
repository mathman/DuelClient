package duelclient;

import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import javax.swing.JPanel;

class Partie extends JPanel implements Runnable
{
    private static Partie instance = null;
    private Thread _t;
    private long _sleepTimer;
    private boolean _running;
    private long _diff;
    private long _oldTime;
    private boolean _UpKey;
    private boolean _DownKey;
    private boolean _LeftKey;
    private boolean _RightKey;
    private boolean _SpaceKey;
    private int _fireTimer;
    private PlayerShip _player;
    private HashMap<Long, PlayerShip> _playersShip;
    private HashMap<Long, MobShip> _mobShip;
    private Map _map;
    
    public static final int WAITING = 1;
    public static final int LOADING = 2;
    public static final int INPROGRESS = 3;

    private Partie()
    {
        _fireTimer = 0;
        _running = false;
        _diff = 0;
        _oldTime = System.currentTimeMillis();
        _sleepTimer = 10;
        _playersShip = new HashMap<>();
        _mobShip = new HashMap<>();
        
        _map = new Map();
        
        addKeyListener(new ClavierListenerPartie());
        this.setFocusable(true);
        setBounds(0, 0, 950, 600);
            
        _SpaceKey = false;
        _UpKey = false;
        _DownKey = false;
        _LeftKey = false;
        _RightKey = false;
        
        _t = new Thread(this);
        _t.start();
    }
    
    public static Partie getInstance()
    {
        if (instance == null)
            instance = new Partie();
           
        return instance;
    }
    
    @Override
    public void run()
    {
        while (!_t.isInterrupted())
        {
            try
            {
                _t.sleep(_sleepTimer);
                
                long currentTime = System.currentTimeMillis();
                _diff = currentTime - _oldTime;

                _oldTime = currentTime;

                if (_running)
                    update(_diff);
            }
            catch (InterruptedException ex) {}
        }
                
    }
    
    private void update(long diff)
    {
        if (_fireTimer <= diff)
        {
            if (_SpaceKey)
            {
                Missile missile = _player.visualFire();
                addVisualMissile(missile);
                Packet packet = new Packet(14, 4 + 4);
                packet.putInt(_player.getPositionX());
                packet.putInt(_player.getPositionY());
                Client.getInstance().sendPacket(packet);
                _fireTimer = 500;
            }
        }
        else
            _fireTimer -= diff;
        
        _map.update();
        repaint();
    }

    @Override
    public void paint(Graphics g)
    {
        super.paint(g);
        _map.drawImage(g);

        Toolkit.getDefaultToolkit().sync();
        g.dispose();
    }
    
    public void onMove(byte stateMove, ObjectMap object)
    {
        switch (object.getObjectType())
        {
            case ObjectMap.MISSILE:
                switch (stateMove)
                {
                    case 0:
                        object.move();
                        break;
                    case 1:
                    case 2:
                        _map.removeObject(object);
                        break;
                }
                break;
            case ObjectMap.MOB:
            case ObjectMap.PLAYER:
                switch (stateMove)
                {
                    case 0:
                        object.move();
                        break;
                }
                break;
        }
    }
    
    public void startGame(Packet messagePacket, PartieWindow window)
    {
        long playerGuid = messagePacket.getLong();
        int score = messagePacket.getInt();

        DuelPlayer player = Client.getInstance().getDuelPlayer(playerGuid);
        if (player != null)
        {
            player.setScore(score);
            window.addPlayerScore(playerGuid);
        }
        
        playerGuid = messagePacket.getLong();
        score = messagePacket.getInt();

        player = Client.getInstance().getDuelPlayer(playerGuid);
        if (player != null)
        {
            player.setScore(score);
            window.addPlayerScore(playerGuid);
        }

        _running = true;
    }
    
    public void updateDelta(Packet messagePacket)
    {
        long Guid = messagePacket.getLong();
        int PositionX = messagePacket.getInt();
        int PositionY = messagePacket.getInt();
        int DeltaX = messagePacket.getInt();
        int DeltaY = messagePacket.getInt();
        ObjectMap Object = _map.getObjectMoveByGuid(Guid);
        if (Object != null)
        {
            Object.setDelta(DeltaX, DeltaY);
            Object.setPosition(PositionX, PositionY);
        }
    }
    
    public void updatePosition(Packet messagePacket)
    {
        long Guid = messagePacket.getLong();
        int PositionX = messagePacket.getInt();
        int PositionY = messagePacket.getInt();
        ObjectMap Object = _map.getObjectMoveByGuid(Guid);
        if (Object != null)
            Object.setPosition(PositionX, PositionY);
    }
    
    public void updateScore(Packet messagePacket)
    {
        long guid = messagePacket.getLong();
        int score = messagePacket.getInt();
        DuelPlayer player = Client.getInstance().getDuelPlayer(guid);
        if (player != null)
            player.setScore(score);
        
        guid = messagePacket.getLong();
        score = messagePacket.getInt();
        player = Client.getInstance().getDuelPlayer(guid);
        if (player != null)
            player.setScore(score);
    }
    
    public void receiveFire(Packet messagePacket)
    {
        Missile missile = null;
        
        long launcher = messagePacket.getLong();
        int PositionXLauncher = messagePacket.getInt();
        int PositionYLauncher = messagePacket.getInt();
        ObjectMap object = _map.getObjectMoveByGuid(launcher);
        if (object != null)
        {
            object.setPosition(PositionXLauncher, PositionYLauncher);
            switch (object.getObjectType())
            {
                case ObjectMap.PLAYER:
                    PlayerShip playerShip = getPlayerShipByGuid(launcher);
                    if (playerShip != null)
                        missile = playerShip.visualFire();
                    break;
                case ObjectMap.MOB:
                    MobShip mobShip = getMobShipByGuid(launcher);
                    if (mobShip != null)
                        missile = mobShip.visualFire();
                    break;
            }
        }
        if (missile != null)
            addVisualMissile(missile);
    }
    
    public void addVisualMissile(Missile missile)
    {
        _map.addObject(missile);
    }
    
    public void addPlayerShip(long guidClient, Packet messagePacket)
    {
        long playerGuid = messagePacket.getLong();
        long ShipGuid = messagePacket.getLong();
        int life = messagePacket.getInt();
        byte orientation = messagePacket.getByte();
        byte LengthMove = messagePacket.getByte();
        byte weaponType = messagePacket.getByte();
        byte Team = messagePacket.getByte();
        byte Type = messagePacket.getByte();
        int PositionX = messagePacket.getInt();
        int PositionY = messagePacket.getInt();
        PlayerShip ship = null;
        if (playerGuid == guidClient)
        {
            _player = new PlayerShip(orientation, ShipGuid, LengthMove, life, weaponType, Team, Type, PositionX, PositionY);
            ship = _player;
        }
        else
            ship = new PlayerShip(orientation, ShipGuid, LengthMove, life, weaponType, Team, Type, PositionX, PositionY);
        
        if (ship != null)
        {
            _map.addObject(ship);
            addPlayer(ship);
        }
    }
    
    public void addMob(Packet messagePacket)
    {
        long shipGuid = messagePacket.getLong();
        int life = messagePacket.getInt();
        byte orientation = messagePacket.getByte();
        byte LengthMove = messagePacket.getByte();
        byte weaponType = messagePacket.getByte();
        byte Team = messagePacket.getByte();
        byte Type = messagePacket.getByte();
        int PositionX = messagePacket.getInt();
        int PositionY = messagePacket.getInt();
        MobShip ship = new MobShip(orientation, weaponType, LengthMove, life, Team, shipGuid, Type, PositionX, PositionY);
        _map.addObject(ship);
        addMob(ship);
    }
    
    public void destroyObject(Packet messagePacket)
    {
        long guid = messagePacket.getLong();
        ObjectMap object = _map.getObjectMoveByGuid(guid);
        switch (object.getObjectType())
        {
            case ObjectMap.PLAYER:
                removePlayer(guid);
                break;
            case ObjectMap.MOB:
                removeMob(guid);
                break;
        }
        _map.removeObject(object);
    }
    
    private void addPlayer(PlayerShip player)
    {
        if (!_playersShip.containsValue(player))
        {
            _playersShip.put(player.getGuid(), player);
        }
    }
    
    private void removePlayer(long guid)
    {
        if (_player != null)
        {
            if (_player.getGuid() == guid)
                _player = null;
        }
        
        if (_playersShip.containsKey(guid))
        {
            _playersShip.remove(guid);
        }
    }
    
    private void addMob(MobShip mob)
    {
        if (!_mobShip.containsValue(mob))
        {
            _mobShip.put(mob.getGuid(), mob);
        }
    }
    
    private void removeMob(long guid)
    {
        if (_mobShip.containsKey(guid))
        {
            _mobShip.remove(guid);
        }
    }
    
    private PlayerShip getPlayerShipByGuid(long guid)
    {
        return _playersShip.get(guid);
    }
    
    private MobShip getMobShipByGuid(long guid)
    {
        return _mobShip.get(guid);
    }
    
    public Packet moveUp(boolean Pressed)
    {
        if (Pressed)
            _player.setDelta(0, -_player.getLengthMove());
        else
            _player.setDelta(0, 0);
        
        Packet packet = new Packet(13, 8 + 4 + 4 + 4 + 4);
        packet.putLong(_player.getGuid());
        packet.putInt(_player.getPositionX());
        packet.putInt(_player.getPositionY());
        packet.putInt(_player.getDeltaX());
        packet.putInt(_player.getDeltaY());
        return packet;
    }
    
    public Packet moveDown(boolean Pressed)
    {
        if (Pressed)
            _player.setDelta(0, _player.getLengthMove());
        else
            _player.setDelta(0, 0);
        
        Packet packet = new Packet(13, 8 + 4 + 4 + 4 + 4);
        packet.putLong(_player.getGuid());
        packet.putInt(_player.getPositionX());
        packet.putInt(_player.getPositionY());
        packet.putInt(_player.getDeltaX());
        packet.putInt(_player.getDeltaY());
        return packet;
    }
    
    public Packet moveLeft(boolean Pressed)
    {
        if (Pressed)
            _player.setDelta(-_player.getLengthMove(), 0);
        else
            _player.setDelta(0, 0);
        
        Packet packet = new Packet(13, 8 + 4 + 4 + 4 + 4);
        packet.putLong(_player.getGuid());
        packet.putInt(_player.getPositionX());
        packet.putInt(_player.getPositionY());
        packet.putInt(_player.getDeltaX());
        packet.putInt(_player.getDeltaY());
        return packet;
    }
    
    public Packet moveRight(boolean Pressed)
    {
        if (Pressed)
            _player.setDelta(_player.getLengthMove(), 0);
        else
            _player.setDelta(0, 0);
        
        Packet packet = new Packet(13, 8 + 4 + 4 + 4 + 4);
        packet.putLong(_player.getGuid());
        packet.putInt(_player.getPositionX());
        packet.putInt(_player.getPositionY());
        packet.putInt(_player.getDeltaX());
        packet.putInt(_player.getDeltaY());
        return packet;
    }
    
    public void killThread()
    {
        _t.interrupt();
        this.removeAll();
    }
  
    private class ClavierListenerPartie extends KeyAdapter
    {
        @Override
        public void keyPressed(KeyEvent event)
        {
            if (_player == null)
                return;
            
            switch (event.getKeyCode())
            {
                  case KeyEvent.VK_SPACE:
                    if (_SpaceKey == false)
                        _SpaceKey = true;
                    break;
                case KeyEvent.VK_UP:
                    if (_UpKey == false)
                    {
                        _UpKey = true;
                        Packet packet = moveUp(true);
                        Client.getInstance().sendPacket(packet);
                    }
                    break;
                case KeyEvent.VK_DOWN:
                    if (_DownKey == false)
                    {
                        _DownKey = true;
                        Packet packet = moveDown(true);
                        Client.getInstance().sendPacket(packet);
                    }
                    break;
                case KeyEvent.VK_LEFT:
                    if (_LeftKey == false)
                    {
                        _LeftKey = true;
                        Packet packet = moveLeft(true);
                        Client.getInstance().sendPacket(packet);
                    }
                    break;
                case KeyEvent.VK_RIGHT:
                    if (_RightKey == false)
                    {
                        _RightKey = true;
                        Packet packet = moveRight(true);
                        Client.getInstance().sendPacket(packet);
                    }
                    break;
            }
                
        }

        @Override
        public void keyReleased(KeyEvent event)
        {
            if (_player == null)
                return;
            
            switch (event.getKeyCode())
            {
                case KeyEvent.VK_SPACE:
                    _SpaceKey = false;
                    break;
                case KeyEvent.VK_UP:
                    _UpKey = false;
                    if (!_UpKey && !_DownKey && !_LeftKey && !_RightKey)
                    {
                        Packet packet = moveUp(false);
                        Client.getInstance().sendPacket(packet);
                    }
                    break;
                case KeyEvent.VK_DOWN:
                    _DownKey = false;
                    if (!_UpKey && !_DownKey && !_LeftKey && !_RightKey)
                    {
                        Packet packet = moveDown(false);
                        Client.getInstance().sendPacket(packet);
                    }
                    break;
                case KeyEvent.VK_LEFT:
                    _LeftKey = false;
                    if (!_UpKey && !_DownKey && !_LeftKey && !_RightKey)
                    {
                        Packet packet = moveLeft(false);
                        Client.getInstance().sendPacket(packet);
                    }
                    break;
                case KeyEvent.VK_RIGHT:
                    _RightKey = false;
                    if (!_UpKey && !_DownKey && !_LeftKey && !_RightKey)
                    {
                        Packet packet = moveRight(false);
                        Client.getInstance().sendPacket(packet);
                    }
                    break;
            }
        }
    }
}