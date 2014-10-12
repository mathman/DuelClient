package duelclient;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;

public class DynamicPlayer extends AbstractTableModel
{
    private List<DuelPlayer> _players;

    private String[] entetes = {"Nom du joueur", "Type de vaisseau", "Pret?"};

    public DynamicPlayer()
    {
        super();
        _players = new ArrayList<>();
    }

    @Override
    public int getRowCount()
    {
        return _players.size();
    }

    @Override
    public int getColumnCount()
    {
        return entetes.length;
    }

    @Override
    public String getColumnName(int columnIndex)
    {
        return entetes[columnIndex];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex)
    {
        switch(columnIndex)
        {
            case 0:
                return _players.get(rowIndex).getName();
            case 1:
                switch (_players.get(rowIndex).getShipType())
                {
                    case 0:
                        return "Terminator";
                    case 1:
                        return "Massacreur";
                }
            case 2:
                switch(_players.get(rowIndex).getReady())
                {
                    case 0:
                        return false;
                    case 1:
                        return true;
                }
            default:
                return null; //Ne devrait jamais arriver
        }
    }
    
    @Override
    public Class getColumnClass(int col)
    {
        switch(col)
        {
            case 0:
            case 1:
                return String.class;
            case 2:
                return Boolean.class;
            default:
                return String.class;
        }
    }
    
    @Override
    public boolean isCellEditable(int row, int col)
    {
        switch(col)
        {
            case 0:
                return false;
            case 1:
            case 2:
                if (_players.get(row).getReady() == 0)
                {
                    if (Client.getInstance().getClientThread().getGuid() == _players.get(row).getGuid())
                        return true;
                }
                return false;
            default:
                return false;
        }
    }
    
    @Override
    public void setValueAt(Object val, int row, int col)
    {
        switch (col)
        {
            case 0:
                break;
            case 1:
                byte type = 0;
                switch (val.toString())
                {
                    case "Terminator":
                        type = 0;
                        break;
                    case "Massacreur":
                        type = 1;
                        break;
                }
                Client.getInstance().sendRequestChangeClass(type);
                break;
            case 2:
                Client.getInstance().sendRequestReady();
                break;
            default:
                break;
        }
    }
    
    public void modifyValue(long playerGuid, int col, int value)
    {
        int i = 0;
        for (i = 0; i < getRowCount(); i++)
        {
            if (_players.get(i).getGuid() == playerGuid)
            {
                switch (col)
                {
                    case 0:
                    case 1:
                        _players.get(i).setShipType((byte) value);
                        break;
                    case 2:
                        _players.get(i).setReady((byte) value);
                        break;
                    default:
                        break;
                }
                fireTableCellUpdated(i, col);
            }
        }
    }

    public void addGamer(DuelPlayer player)
    {
        _players.add(player);

        fireTableRowsInserted(_players.size() -1, _players.size() -1);
    }

    public void removePlayer(int rowIndex)
    {
        _players.remove(rowIndex);

        fireTableRowsDeleted(rowIndex, rowIndex);
    }
    
    public void removeAllPlayers()
    {
        int rows = getRowCount();
        while (rows != 0)
        {
            removePlayer(0);
            rows = getRowCount();
        }
    }
    
    public DuelPlayer getDuelPlayerByGuid(long guid)
    {
        for (int i = 0; i < _players.size(); i++)
        {
            if (_players.get(i).getGuid() == guid)
                return _players.get(i);
        }
        return null;
    }
}
