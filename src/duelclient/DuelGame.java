package duelclient;

public class DuelGame
{
    private String _name;
    private int _nbPlayers;
    private int _etat;
    private byte _type;
    
    public static final byte JCE = 0;
    public static final byte JCJ = 1;
    
    public DuelGame(String name, byte type, int nbplayers, int etat)
    {
        _type = type;
        _name = name;
        _nbPlayers = nbplayers;
        _etat = etat;
    }
    
    public String getName()
    {
        return _name;
    }

    public void setName(String name)
    {
        _name = name;
    }

    public int getNbPlayers()
    {
        return _nbPlayers;
    }

    public void setNbPlayers(int nbPlayers)
    {
        _nbPlayers = nbPlayers;
    }

    public int getState()
    {
        return _etat;
    }

    public void setState(int state)
    {
        _etat = state;
    }
    
    public byte getType()
    {
        return _type;
    }

    public void setType(byte type)
    {
        _type = type;
    }
}
