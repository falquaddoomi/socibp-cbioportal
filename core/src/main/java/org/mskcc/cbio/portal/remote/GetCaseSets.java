/** Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
**
** This library is free software; you can redistribute it and/or modify it
** under the terms of the GNU Lesser General Public License as published
** by the Free Software Foundation; either version 2.1 of the License, or
** any later version.
**
** This library is distributed in the hope that it will be useful, but
** WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
** MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
** documentation provided hereunder is on an "as is" basis, and
** Memorial Sloan-Kettering Cancer Center 
** has no obligations to provide maintenance, support,
** updates, enhancements or modifications.  In no event shall
** Memorial Sloan-Kettering Cancer Center
** be liable to any party for direct, indirect, special,
** incidental or consequential damages, including lost profits, arising
** out of the use of this software and its documentation, even if
** Memorial Sloan-Kettering Cancer Center 
** has been advised of the possibility of such damage.  See
** the GNU Lesser General Public License for more details.
**
** You should have received a copy of the GNU Lesser General Public License
** along with this library; if not, write to the Free Software Foundation,
** Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
**/

package org.mskcc.cbio.portal.remote;

import org.mskcc.cbio.portal.dao.DaoCaseList;
import org.mskcc.cbio.portal.dao.DaoCancerStudy;
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.model.CancerStudy;
import org.mskcc.cbio.portal.model.CaseList;

import java.util.ArrayList;

/**
 * Gets all Case Sets Associated with a specific Cancer Type.
 */
public class GetCaseSets {

    /**
     * Gets all Case Sets Associated with a specific Cancer Study.
     *
     * @param cancerStudyId Cancer Study ID.
     * @return ArrayList of CaseSet Objects.
     * @throws DaoException Database Error.
     */
    public static ArrayList<CaseList> getCaseSets(String cancerStudyId)
            throws DaoException {
        CancerStudy cancerStudy = DaoCancerStudy.getCancerStudyByStableId(cancerStudyId);
        if (cancerStudy != null) {
            DaoCaseList daoCaseList = new DaoCaseList();
            ArrayList<CaseList> caseList = daoCaseList.getAllCaseLists(cancerStudy.getInternalId());
            return caseList;
        } else {
            ArrayList<CaseList> caseList = new ArrayList<CaseList>();
            return caseList;
        }
    }
}
