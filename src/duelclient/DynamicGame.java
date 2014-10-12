package duelclient;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;

public class DynamicGame extends AbstractTableModel
{
    private final List<DuelGame> _servers = new ArrayList<>();

    private final String[] entetes = {"Nom de la partie", "Nombre de joueurs", "Type", "Etat"};

    public DynamicGame()
    {
        super();
    }

    @Override
    public int getRowCount()
    {
        return _servers.size();
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
                return _servers.get(rowIndex).getName();
            case 1:
                String players = String.valueOf(_servers.get(rowIndex).getNbPlayers());
                return players + "/2";
            case 2:
                switch (_servers.get(rowIndex).getType())
                {
                    case DuelGame.JCE:
                        return "JCE";
                    case DuelGame.JCJ:
                        return "JCJ";
                    default:
                        return "Erreur";
                }
            case 3:
                switch (_servers.get(rowIndex).getState())
                {
                    case Partie.WAITING:
                        return "En attente";
                    case Partie.LOADING:
                        return "En chargement";
                    case Partie.INPROGRESS:
                        return "En cours";
                    default:
                        return "Erreur";
                }
            default:
                return null; //Ne devrait jamais arriver
        }
    }

    public void addServer(DuelGame server)
    {
        _servers.add(server);

        fireTableRowsInserted(_servers.size() -1, _servers.size() -1);
    }

    public void removeServer(int rowIndex)
    {
        _servers.remove(rowIndex);

        fireTableRowsDeleted(rowIndex, rowIndex);
    }
    
    public void removeAllServer()
    {
        int rows = getRowCount();
        while (rows != 0)
        {
            removeServer(0);
            rows = getRowCount();
        }
    }
}
