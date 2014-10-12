package duelclient;

import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class PlayerShip extends ObjectMap
{
    private byte _weaponType;
    
    PlayerShip(byte orientation, long Guid, byte LengthMove, int Life, byte weaponType, byte Team, byte Type, int PositionX, int PositionY)
    {
        super(orientation, Team, Guid, Type, PositionX, PositionY);
        _LengthMove = LengthMove;
        _Life = Life;
        _objectType = ObjectMap.PLAYER;
        _weaponType = weaponType;
        switch (_type)
        {
            case 0:
                _imageIcon = "vaisseau1.png";
                _SizeX = 50;
                _SizeY = 50;
                break;
            case 1:
                _imageIcon = "vaisseau2.png";
                _SizeX = 50;
                _SizeY = 50;
                break;
        }

        try
        {
            _image = ImageIO.read(new File(_imageIcon));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    
    public byte getWeaponType()
    {
        return _weaponType;
    }
    
    public Missile visualFire()
    {
        byte weaponType = getWeaponType();
        byte team = getTeam();
        int PositionY = 0;
        int PositionX = 0;
        int DeltaX = 0;
        int DeltaY = 0;
        switch (weaponType)
        {
            case 0:
                DeltaX = 0;
                DeltaY = -5;
                PositionY = getPositionY() + 25;
                PositionX = getPositionX() + 20;
                break;
        }
        byte orientation = _orientation;
        Missile missile = new Missile(orientation, team, -1, weaponType , PositionX, PositionY);
        missile.setDelta(DeltaX, DeltaY);
        return missile;
    }
}