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
 * @author Sander Tan
 * code based on TestImportGeneData.java
*/

package org.mskcc.cbio.portal.scripts;

import org.cbioportal.model.GeneSet;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.mskcc.cbio.portal.dao.DaoGeneSet;

import org.mskcc.cbio.portal.util.ProgressMonitor;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

/*
 * JUnit tests for ImportGeneSetData class.
*/

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/applicationContext-dao.xml" })
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = true)
@Transactional
public class TestImportGeneSetData {

	@Test
    public void testImportGeneSetData() throws Exception {
		// Initiate daoGeneSet to connect to database
    	DaoGeneSet daoGeneSet = DaoGeneSet.getInstance();
        ProgressMonitor.setConsoleMode(false);
        
        // Open genesets test data file
        File file = new File("src/test/resources/genesets_test.txt");
        boolean allowUpdates = true;
        String version = "";
        int skippedGenes = ImportGeneSetData.importData(file, allowUpdates, version);

        // Open supplementary file
        file = new File("src/test/resources/supp-genesets.txt");
        ImportGeneSetData.importSuppGeneSetData(file);
        
        // Test database entries
        GeneSet geneSet = daoGeneSet.getGeneSetByExternalId("UNITTEST_GENESET5");
        assertEquals("UNITTEST_GENESET5", geneSet.getExternalId());
        geneSet = daoGeneSet.getGeneSetByExternalId("UNITTEST_GENESET10");
        assertEquals("http://www.broadinstitute.org/gsea/msigdb/cards/GCNP_SHH_UP_EARLY.V1_UP", geneSet.getRefLink());
        
        // Test warning message
        assertEquals(5, skippedGenes);
        
        // Test database entries supplementary file
        geneSet = daoGeneSet.getGeneSetByExternalId("UNITTEST_GENESET2");
        assertEquals("Genes up-regulated in RK3E cells (kidney epithelium) over-expressing GLI1 [GeneID=2735].", geneSet.getName());
        geneSet = daoGeneSet.getGeneSetByExternalId("UNITTEST_GENESET8");
        assertEquals("UNITTEST_GENESET8", geneSet.getNameShort());
    }
}

