package org.mskcc.cbio.portal.scripts;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mskcc.cbio.portal.dao.*;
import org.mskcc.cbio.portal.model.ClinicalAttribute;
import org.mskcc.cbio.portal.model.TypeOfCancer;
import org.mskcc.cbio.portal.util.*;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/applicationContext-dao.xml" })
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = true)
@Transactional
public class TestPanCancerImport {

    private ProgressMonitor pMonitor = new ProgressMonitor();

    // This test depends on the data seed_mini_pancancer.sql being loaded first
    @Test
    public void testPanCancerImport() throws Exception {
        pMonitor.setConsoleMode(true);

        // have to do add the pan_cancer cancer type first
        addCancerType("pan_cancer", "Pan-Cancer", "other", "Red", "PANCAN", "tissue");

        // ImportCancerStudy portal-study/meta_study.txt
        addCancerStudy("src/test/resources/pan-cancer/meta_study.txt");

        // ImportProfileData --meta portal-study/meta_CNA.txt --loadMode bulkload --data portal-study/data_CNA.txt
        addProfileData("src/test/resources/pan-cancer/meta_CNA.txt", "src/test/resources/pan-cancer/data_CNA.txt");

        // ImportProfileData --meta portal-study/meta_mutations_extended.txt --loadMode bulkload --data portal-study/data_mutations_extended.txt
        addProfileData("src/test/resources/pan-cancer/meta_mutations_extended.txt", "src/test/resources/pan-cancer/data_mutations_extended.txt");

        // ImportPatientList portal-study/case_lists/cases_all.txt
        // we're passing a single file, not the directory
        addPatientLists("src/test/resources/pan-cancer/cases_all.txt");

        //ImportClinicalData portal-study/data_clinical.txt multi_cancer_study
        addClinicalData("src/test/resources/pan-cancer/data_clinical.txt", "multi_cancer_study");

        // test getCancerTypeInfo
        testGetCancerTypeInfo();
    }

    private void addCancerType(String cancerID, String name, String keyword, String color, String shortName, String parentID) throws Exception{
            TypeOfCancer aTypeOfCancer = new TypeOfCancer();
            aTypeOfCancer.setTypeOfCancerId(cancerID);
            aTypeOfCancer.setName(name);
            aTypeOfCancer.setClinicalTrialKeywords(keyword);
            aTypeOfCancer.setDedicatedColor(color);
            aTypeOfCancer.setShortName(shortName);
            aTypeOfCancer.setParentTypeOfCancerId(parentID);
            DaoTypeOfCancer.addTypeOfCancer(aTypeOfCancer);
    }

    private void addCancerStudy(String fileName) throws Exception{
        ImportCancerStudy.main(new String[]{fileName});
    }

    private void addProfileData(String descriptorFileName, String dataFileName) throws Exception{
        ImportProfileData.main(new String[]{"--meta", descriptorFileName, "--loadMode", "bulkload", "--data", dataFileName});
    }

    private void addPatientLists(String fileName) throws Exception{
        ImportPatientList.main(new String[]{fileName});
    }

    private void addClinicalData(String dataFileName, String studyID) throws Exception{
        ImportClinicalData.main(new String[]{dataFileName, studyID});
    }

//    @Test
    public void testGetCancerTypeInfo() throws Exception{
        String studyName="multi_cancer_study";
        int cancerId = DaoCancerStudy.getCancerStudyByStableId(studyName).getInternalId();

        // retrieve the cancerTypeInfoMap for the study
        // this should contain:
        // 'CANCER_TYPE' - 'cns', 'cervix'
        // 'CANCER_TYPE_DETAILED' - 'byst', 'bimt', 'cead', 'cene', 'cacc'
        Map<String, List<String>> cancerTypeInfoMap = DaoClinicalData.getCancerTypeInfo(cancerId);
        assertEquals(cancerTypeInfoMap.keySet().size(), 2);
        assertEquals(cancerTypeInfoMap.containsKey(ClinicalAttribute.CANCER_TYPE), true);
        assertEquals(cancerTypeInfoMap.containsKey(ClinicalAttribute.CANCER_TYPE_DETAILED), true);
        assertEquals(cancerTypeInfoMap.get(ClinicalAttribute.CANCER_TYPE).size(), 2);
        assertEquals(cancerTypeInfoMap.get(ClinicalAttribute.CANCER_TYPE_DETAILED).size(), 5);

    }
}
