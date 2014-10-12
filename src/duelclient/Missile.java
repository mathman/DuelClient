package duelclient;

import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class Missile extends ObjectMap
{
    Missile(byte orientation, byte Team, long Guid, int Type, int PositionX, int PositionY)
    {
        super(orientation, Team, Guid, Type, PositionX, PositionY);
        _objectType = ObjectMap.MISSILE;
        switch (_type)
        {
            case 0:
                _LengthMove = 5;
                _Life = 1;
                _imageIcon = "missile.png";
                _SizeX = 10;
                _SizeY = 25;
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
}