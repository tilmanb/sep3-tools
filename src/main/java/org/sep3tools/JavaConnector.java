package org.sep3tools;

import java.sql.*;
import java.util.logging.Logger;

/**
 * This class connects and communicates with a SEP3 Database to execute the queries and to retrieve translations for
 * SEP3-Codes, quantifiers, etc.
 *
 * @author Jeronimo Wanhoff <kontakt@jeronimowanhoff.de>
 * @author Thorsten Friebe
 */
public final class JavaConnector {

    private static final Logger LOG = Logger.getLogger( JavaConnector.class.getName() );

    private static String m_url = "jdbc:default:connection";

    private static String user = "";

    private static String pass = "";

    private static String wb = "woerterbuch.\"Woerterbuch\"";

    private static String st = "woerterbuch.\"Schluesseltypen\"";

    private JavaConnector() {
    }

    public static void setWb( String wb ) {
        JavaConnector.wb = wb;
    }

    public static void setSt( String st ) {
        JavaConnector.st = st;
    }

    public static void setUser( String user ) {
        JavaConnector.user = user;
    }

    public static void setPass( String pass ) {
        JavaConnector.pass = pass;
    }

    public static void setUrl( String url ) {
        JavaConnector.m_url = url;
    }

    //String query = "SELECT Klartext from woerterbuch.Woerterbuch where Kuerzel=";

    /**
     * translates a SEP3 code to clear text
     *
     * @param sep3Code code for translation
     * @return translated SEP3 string
     * @throws SQLException if DB error occurs
     */
    public static String getS3Name( String sep3Code )
                            throws SQLException {
        Connection conn = DriverManager.getConnection( m_url, user, pass );
        String query = "select \"Kuerzel\", \"Klartext\" from " + wb + " w join " + st + " s "
                       + "on w.\"Typ\" = s.\"Nebentypbez\" "
                       + "where (s.\"Datenfeld\" = 'PETRO' OR s.\"Datenfeld\" = 'diverse') AND \"Kuerzel\"= ?";

        PreparedStatement stmt = conn.prepareStatement( query );
        stmt.setString( 1, sep3Code );
        LOG.fine( "Executing statement: " + stmt );
        try (ResultSet rs = stmt.executeQuery()) {
            boolean validRS = rs.next();
            String result = "";
            if ( validRS ) {
                result = rs.getString( 2 );
            }
            LOG.fine( "Returning: " + result );
            return result;
        }
    }

    /**
     * Retrieves allowed attributes for a given SEP3 code
     *
     * @param sep3Code for which attributes are requested
     * @return String containing information about allowed attributes, quantifiers, etc.
     * @throws SQLException
     *             if DB error occurs
     */
    public static String getAllowedAttribs( String sep3Code )
                            throws SQLException {
        Connection conn = DriverManager.getConnection( m_url, user, pass );
        String query = "select \"Kuerzel\", \"Attribute\" from " + wb + " w join " + st + " s "
                       + "on w.\"Typ\" = s.\"Nebentypbez\" "
                       + "where (s.\"Datenfeld\" = 'PETRO' OR s.\"Datenfeld\" = 'diverse') AND \"Kuerzel\"= ?";
        PreparedStatement stmt = conn.prepareStatement( query );
        stmt.setString( 1, sep3Code );
        LOG.fine( "Executing statement: " + stmt );
        try (ResultSet rs = stmt.executeQuery()) {
            boolean validRS = rs.next();
            String result = "";
            if ( validRS ) {
                result = rs.getString( 2 );
            }
            LOG.fine( "Returning: " + result );
            return result;
        }
    }

    /**
     * returns appropriate quantifier string for a SEP3 code and quantifier (as digit)
     *
     * @param sep3Code that is quantified with quant
     * @param quant quantifyer for sep3code (as digit)
     * @return quantifier based on sep3 code and quantifyer
     * @throws SQLException in case of DB error
     */
    public static String getBodenQuant( String sep3Code, String quant )
                            throws SQLException {
        String allowedAttributes;
        String quantBez;

        allowedAttributes = getAllowedAttribs( sep3Code );
        quantBez = getQuantBezFromAttribs( allowedAttributes );

        Connection conn = DriverManager.getConnection( m_url, user, pass );
        String query = "select w.\"Kuerzel\", w.\"Klartext\", s.\"Nebentypbez\" from " + wb + " w join " + st + " s "
                       + "on w.\"Typ\" = s.\"Nebentypbez\" where (s.\"Nebentypbez\" = ? AND w.\"Kuerzel\" = ?);";
        PreparedStatement stmt = conn.prepareStatement( query );
        stmt.setString( 1, quantBez );
        stmt.setString( 2, quant );
        LOG.fine( "Executing statement: " + stmt );
        try (ResultSet rs = stmt.executeQuery()) {
            boolean validRS = rs.next();
            String result = "";
            if ( validRS ) {
                result = rs.getString( 2 );
            }
            LOG.fine( "Returning: " + result );

            return result;
        }
    }

    /**
     * extracts quantifyer type from a  comma separated list of attributes
     *
     * @param attributes as comma separated list
     * @return quantifyer type
     */
    public static String getQuantBezFromAttribs( String attributes ) {
        String[] attribs;
        attribs = attributes.split( "," );
        for ( int i = 0; i < attribs.length; i++ ) {
            if ( attribs[i].startsWith( "Quant_" ) ) {
                return attribs[i];
            }
        }
        return "";
    }

}
