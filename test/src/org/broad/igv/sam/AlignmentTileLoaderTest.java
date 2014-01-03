/*
 * Copyright (c) 2007-2012 The Broad Institute, Inc.
 * SOFTWARE COPYRIGHT NOTICE
 * This software and its documentation are the copyright of the Broad Institute, Inc. All rights are reserved.
 *
 * This software is supplied without any warranty or guaranteed support whatsoever. The Broad Institute is not responsible for its use, misuse, or functionality.
 *
 * This software is licensed under the terms of the GNU Lesser General Public License (LGPL),
 * Version 2.1 which is available at http://www.opensource.org/licenses/lgpl-2.1.php.
 */

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.broad.igv.sam;

import org.broad.igv.AbstractHeadlessTest;
import org.broad.igv.PreferenceManager;
import org.broad.igv.sam.reader.AlignmentReader;
import org.broad.igv.sam.reader.AlignmentReaderFactory;
import org.broad.igv.util.ResourceLocator;
import org.broad.igv.util.TestUtils;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * @author jrobinso
 */
public class AlignmentTileLoaderTest extends AbstractHeadlessTest {


    /**
     * Test that sampling keeps pairs together.
     *
     * @throws Exception
     */
    @Test
    //@Ignore("Not implemented yet")
    public void testKeepPairsDownsample_01() throws Exception {
        String path = "http://1000genomes.s3.amazonaws.com/data/NA12878/high_coverage_alignment/NA12878.mapped.ILLUMINA.bwa.CEU.high_coverage_pcr_free.20130520.bam";

        String sequence = "1";
        int start = 10000-1;
        int end = 11000;
        int maxDepth = -1;

        AlignmentTileLoader.AlignmentTile tile= tstKeepPairsDownsample(path, sequence, start, end, maxDepth);
        assertTrue(tile.getDownsampledIntervals().size() > 0);
    }

    /**
     * Test that sampling keeps pairs together.
     *
     * @throws Exception
     */
    @Test
    public void testKeepPairsDownsample_02() throws Exception {
        String path = TestUtils.LARGE_DATA_DIR + "HG00171.hg18.bam";

        String sequence = "chr1";
        int start = 151766945;
        int end = 151791197;
        int maxDepth = 5;

        AlignmentTileLoader.AlignmentTile tile= tstKeepPairsDownsample(path, sequence, start, end, maxDepth);
        assertTrue(tile.getDownsampledIntervals().size() > 0);
    }

    /**
     * Test that sampling keeps pairs together.
     *
     * @throws Exception
     */
    @Test
    public void testNoDownsample() throws Exception {
        String path = TestUtils.LARGE_DATA_DIR + "HG00171.hg18.bam";

        String sequence = "chr1";
        int start = 151766945;
        int end = 151791197;
        int maxDepth = 500;

        AlignmentTileLoader.AlignmentTile tile= tstKeepPairsDownsample(path, sequence, start, end, maxDepth);
        assertEquals(0, tile.getDownsampledIntervals().size());


    }

    private AlignmentTileLoader.AlignmentTile tstKeepPairsDownsample(String path, String sequence, int start, int end, int maxDepth) throws Exception{


        String oldMaxVis = PreferenceManager.getInstance().get(PreferenceManager.SAM_MAX_VISIBLE_RANGE);
        PreferenceManager.getInstance().put(PreferenceManager.SAM_MAX_VISIBLE_RANGE, "" + (end - start));

        int actMaxDepth = 100;
        if(maxDepth > 0){
            actMaxDepth = maxDepth;
        }

        try {
            ResourceLocator loc = new ResourceLocator(path);
            AlignmentReader reader = AlignmentReaderFactory.getReader(loc);
            AlignmentTileLoader loader = new AlignmentTileLoader(reader);

            AlignmentDataManager.DownsampleOptions downsampleOptions = new AlignmentDataManager.DownsampleOptions(true, 50, actMaxDepth);

            AlignmentTileLoader.AlignmentTile tile = loader.loadTile(sequence, start, end, null, downsampleOptions, null, null, null);
            List<Alignment> alignments = tile.getAlignments();
            int count = 0;
            Map<String, Integer> pairedReads = new HashMap<String, Integer>();
            for(Alignment al: alignments) {
                assertNotNull(al);
                count++;

                if (al.isPaired() && al.getMate().isMapped()) {
                    //Mate may not be part of the query.
                    //Make sure it's within bounds
                    int mateStart = al.getMate().getStart();
                    //All we require is some overlap
                    boolean overlap = (mateStart + al.getReadSequence().length()) >= start && mateStart < end;
                    overlap &= al.getMate().getChr().equals(al.getChr());
                    if (overlap) {
                        Integer rdCnt = pairedReads.get(al.getReadName());
                        rdCnt = rdCnt != null ? rdCnt + 1 : 1;
                        pairedReads.put(al.getReadName(), rdCnt);
                    }
                }
            }

            assertTrue("No alignments loaded", count > 0);

            int countmissing = 0;
            for (String readName : pairedReads.keySet()) {
                int val = pairedReads.get(readName);
                countmissing += 2 == val ? 0 : 1;
                if (val != 2) {
                    System.out.println("Read " + readName + " has val " + val);
                }
            }

            System.out.println("Number of paired reads: " + pairedReads.size());
            assertTrue("No pairs in test data set", pairedReads.size() > 0);
            assertEquals("Missing " + countmissing + " out of " + pairedReads.size() + " pairs", 0, countmissing);
            return tile;
        } catch (Exception e) {
            throw e;
        }finally{
            PreferenceManager.getInstance().put(PreferenceManager.SAM_MAX_VISIBLE_RANGE, oldMaxVis);
        }

    }

}