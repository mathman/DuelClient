package duelclient;

import java.awt.Image;
import java.awt.geom.AffineTransform;

public class ObjectMap
{
    private int _PositionX;
    private int _PositionY;
    String _imageIcon;
    protected Image _image;
    protected int _LengthMove;
    private int _DeltaX;
    private int _DeltaY;
    protected int _type;
    private long _Guid;
    protected byte _objectType;
    protected byte _team;
    protected int _SizeX;
    protected int _SizeY;
    protected int _Life;
    protected byte _orientation;
    AffineTransform _rotation;
    
    public static final int PLAYER = 1;
    public static final int MOB = 2;
    public static final int MISSILE = 3;
    
    public static final int UP = 0;
    public static final int RIGHT = 1;
    public static final int DOWN = 2;
    public static final int LEFT = 3;
    
    ObjectMap(byte orientation, byte team, long Guid, int type, int PositionX, int PositionY)
    {
        _rotation = new AffineTransform();
        _orientation = orientation;
        _team = team;
        _Guid = Guid;
        _type = type;
        _DeltaX = 0;
        _DeltaY = 0;
        _PositionX = PositionX;
        _PositionY = PositionY;
    }
    
    public AffineTransform getTransform()
    {
        return _rotation;
    }
    
    public void setOrientation(byte orientation)
    {
        _orientation = orientation;
    }
    
    public byte getOrientation()
    {
        return _orientation;
    }
    
    public long getGuid()
    {
        return _Guid;
    }
    
    public int getLengthMove()
    {
        return _LengthMove;
    }
    
    public void setDelta(int DeltaX, int DeltaY)
    {
        _DeltaX = DeltaX;
        _DeltaY = DeltaY;
    }
    
    public int getDeltaX()
    {
        return _DeltaX;
    }
    
    public int getDeltaY()
    {
        return _DeltaY;
    }
    
    public Image getImage()
    {
        return _image;
    }
    
    public int getPositionX()
    {
        return _PositionX;
    }
    
    public int getPositionY()
    {
        return _PositionY;
    }
    
    public byte getObjectType()
    {
        return _objectType;
    }
    
    public byte getTeam()
    {
        return _team;
    }
    
    public void setPosition(int PositionX, int PositionY)
    {
        _PositionX = PositionX;
        _PositionY = PositionY;
    }
    
    public void move()
    {
        _PositionX = _PositionX + _DeltaX;
        _PositionY = _PositionY + _DeltaY;
    }
    
    public int getSizeX()
    {
        return _SizeX;
    }
    
    public int getSizeY()
    {
        return _SizeY;
    }
    
    public int getLife()
    {
        return _Life;
    }
    
    public void setLife(int Life)
    {
        _Life = Life;
    }
}
