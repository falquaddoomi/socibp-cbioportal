package org.mskcc.cbio.portal.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DaoGeneticEntity {

	/**
     * Private Constructor to enforce Singleton Pattern.
     */
    private DaoGeneticEntity() {
    }
    
    // TODO consider renaming this class to something similar to
    // DaoEntity because the name does not correctly capture the 
    // new Treatment `genetic_entity` type.
    // see: https://github.com/cBioPortal/cbioportal/pull/5460#discussion_r250148672
    public static enum EntityTypes
    {
        GENE,
        GENESET,
        TREATMENT,
        PHOSPHOPROTEIN;
    }
    
    /**
     * Adds a new genetic entity Record to the Database and 
     * returns the auto generated id value.
     *
     * @param entityType : one of ...
     * @return : auto generated genetic entity id value
     * @throws DaoException Database Error.
     */
    public static int addNewGeneticEntity(EntityTypes entityType) throws DaoException {

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoGene.class);
        	pstmt = con.prepareStatement
                    ("INSERT INTO genetic_entity (`ENTITY_TYPE`) "
                            + "VALUES (?)", Statement.RETURN_GENERATED_KEYS);
        	pstmt.setString(1, entityType.name());
            pstmt.executeUpdate();
            //get the auto generated key:
            rs = pstmt.getGeneratedKeys();
            rs.next();
            int newId = rs.getInt(1);
            return newId;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoGene.class, con, pstmt, rs);
        }
    }
}
