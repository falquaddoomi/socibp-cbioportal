/*
 * Copyright (c) 2016 The Hyve B.V.
 */

/*
 * This file is part of cBioPortal.
 *
 * cBioPortal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

/*
 * @author ochoaa
 * @author Sander Tan
*/

package org.mskcc.cbio.portal.dao;

import org.cbioportal.model.GeneSetInfo;
import java.sql.*;

public class DaoGeneSetInfo {
	
	// Keep Constructor empty
	private DaoGeneSetInfo() {
	}
	
	/**
     * Set gene set version in geneset_info table in database.
     * @throws DaoException 
     */
    public static void setGeneSetInfo(GeneSetInfo geneSetInfo) throws DaoException {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        
        try {
        	// Open connection to database
            connection = JdbcUtil.getDbConnection(DaoGeneSetHierarchy.class);
	        
	        // Prepare SQL statement
            preparedStatement = connection.prepareStatement("INSERT INTO geneset_info " 
	                + "(`GENESET_VERSION`) VALUES(?)");	        
            
            // Fill in statement
            preparedStatement.setString(1, "1");
            
            // Execute statement
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoGeneSetInfo.class, connection, preparedStatement, resultSet);
        }
    }	
    	
	/**
     * Set gene set version in geneset_info table in database.
     * @throws DaoException 
     */
    public static void updateGeneSetInfo(GeneSetInfo geneSetInfo) throws DaoException {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        
        try {
        	// Open connection to database
            connection = JdbcUtil.getDbConnection(DaoGeneSetHierarchy.class);
	        
	        // Prepare SQL statement
            preparedStatement = connection.prepareStatement("UPDATE geneset_info SET GENESET_VERSION=" + geneSetInfo.getVersion());
            
            // Execute statement
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoGeneSetInfo.class, connection, preparedStatement, resultSet);
        }
    }	
    
	/**
     * Get gene set version from geneset_info table in database.
     * @throws DaoException 
     */
    public static GeneSetInfo getGeneSetInfo() throws DaoException {
    	
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        
        try {
        	// Open connection to database
        	connection = JdbcUtil.getDbConnection(DaoGeneSetInfo.class);
        	
	        // Prepare SQL statement
        	preparedStatement = connection.prepareStatement(
        			"SELECT * FROM geneset_info");
        	
            // Execute statement
        	resultSet = preparedStatement.executeQuery();
        	GeneSetInfo geneSetInfo = new GeneSetInfo();

            // Extract version from result
            if (resultSet.next()) {
                geneSetInfo.setVersion(resultSet.getString("GENESET_VERSION"));
            }    
        	return geneSetInfo;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoGeneSetInfo.class, connection, preparedStatement, resultSet);
        }
    }
}


