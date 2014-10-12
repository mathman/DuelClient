package duelclient;

public class DuelPlayer
{
    private String _name;
    private byte _class;
    private byte _ready;
    private long _guid;
    private int _score;
    
    DuelPlayer(long guid, String name, byte Class, byte ready)
    {
        _score = 0;
        _guid = guid;
    	_name = name;
        _class = Class;
        _ready = ready;
    }
    
    public void setScore(int score)
    {
        _score = score;
    }
    
    public int getScore()
    {
        return _score;
    }

    public void setName(String name)
    {
    	_name = name;
    }
    
    public String getName()
    {
    	return _name;
    }
    
    public void setShipType(byte classGame)
    {
    	_class = classGame;
    }
    
    public byte getShipType()
    {
    	return _class;
    }
    
    public void setReady(byte ready)
    {
    	_ready = ready;
    }
    
    public byte getReady()
    {
    	return _ready;
    }
    
    public long getGuid()
    {
        return _guid;
    }
}
