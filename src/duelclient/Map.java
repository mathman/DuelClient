package duelclient;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

public class Map
{
    private int _NbBlocsX;
    private int _NbBlocsY;
    private List<ObjectMap> _objectMap;
    private Image _background;
    private List<ObjectMap> _guidsDestroy;
    
    Map()
    {
        _NbBlocsX = 965;
        _NbBlocsY = 600;
        _guidsDestroy = new ArrayList<>();
        _objectMap = new ArrayList<>();
        try
        {
            _background = ImageIO.read(new File("background.jpg"));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    
    public byte checkMove(ObjectMap object)
    {
        if (object.getDeltaX() < 0)
        {
            if (object.getPositionX() < 1)
            {
                return 1;
            }
        }
        else if (object.getDeltaX() > 0)
        {
            if (object.getPositionX() + object.getSizeX()  + object.getLengthMove() > _NbBlocsX)
            {
                return 1;
            }
        }
        else if (object.getDeltaY() < 0)
        {
            if (object.getPositionY() < 1)
            {
                return 1;
            }
        }
        else if (object.getDeltaY() > 0)
        {
            if (object.getPositionY() + object.getSizeY()  + object.getLengthMove() > _NbBlocsY)
            {
                return 1;
            }
        }
        
        for (int i = 0; i < _objectMap.size(); i++)
        {
            ObjectMap value = _objectMap.get(i);
            if (object.getTeam() != value.getTeam())
            {
                if (inCollision(object, value))
                    return 2;
            }
        }
        return 0;
    }
    
    private boolean inCollision(ObjectMap firstObject, ObjectMap secondObject)
    {
        if ((secondObject.getPositionX() >= firstObject.getPositionX() + firstObject.getSizeX())
        || (secondObject.getPositionX() + secondObject.getSizeX() <= firstObject.getPositionX())
        || (secondObject.getPositionY() >= firstObject.getPositionY() + firstObject.getSizeY())
        || (secondObject.getPositionY() + secondObject.getSizeY() <= firstObject.getPositionY()))
            return false;
        
        return true;
    }

    public void update()
    {
        for (int i = 0; i < _objectMap.size(); i++)
        {
            ObjectMap object = _objectMap.get(i);
            byte stateMove = checkMove(object);
            Partie.getInstance().onMove(stateMove, object);
        }
    }
    
    public void addObject(ObjectMap object)
    {
        if (!_objectMap.contains(object))
        {
            _objectMap.add(object);
        }
    }
    
    public void removeObject(ObjectMap object)
    {
        if (_objectMap.contains(object))
        {
            _guidsDestroy.add(object);
        }
    }
    
    public void drawImage(Graphics g)
    {
        Graphics2D g2d = (Graphics2D)g;
        g2d.drawImage(_background, 0, 0, _NbBlocsX, _NbBlocsY, null);
        
        for (int i = 0; i < _guidsDestroy.size(); i++)
        {
            _objectMap.remove(_guidsDestroy.get(i));
        }
        _guidsDestroy.clear();
        
        for (int i = 0; i < _objectMap.size(); i++)
        {
            ObjectMap value = _objectMap.get(i);
            switch (value.getOrientation())
            {
                case ObjectMap.UP:
                    g2d.drawImage(value.getImage(), value.getPositionX(), value.getPositionY(), null);
                    break;
                case ObjectMap.RIGHT:
                    value.getTransform().translate(value.getPositionX() - value.getImage().getWidth(null)/2, value.getPositionY() - value.getImage().getHeight(null)/2);
                    value.getTransform().rotate(Math.toRadians(90),(value.getImage().getWidth(null)/2),(value.getImage().getHeight(null)/2));
                    g2d.drawImage(value.getImage(), value.getTransform(), null);
                    break;
                case ObjectMap.DOWN:
                    //value.getTransform().translate(value.getPositionX() - value.getImage().getWidth(null)/2, value.getPositionY() - value.getImage().getHeight(null)/2);
                    value.getTransform().rotate(Math.toRadians(180),(value.getImage().getWidth(null)/2),(value.getImage().getHeight(null)/2));
                    g2d.drawImage(value.getImage(), value.getTransform(), null);
                    break;
                case ObjectMap.LEFT:
                    value.getTransform().translate(value.getPositionX() - value.getImage().getWidth(null)/2, value.getPositionY() - value.getImage().getHeight(null)/2);
                    value.getTransform().rotate(Math.toRadians(270),(value.getImage().getWidth(null)/2),(value.getImage().getHeight(null)/2));
                    g2d.drawImage(value.getImage(), value.getTransform(), null);
                    break;
            }
        }
        g2d.dispose();
    }
    
    public ObjectMap getObjectMoveByGuid(long Guid)
    {
        for (int i = 0; i < _objectMap.size(); i++)
        {
            ObjectMap value = _objectMap.get(i);
            if (value.getGuid() == Guid)
                return value;
        }
        return null;
    }
}