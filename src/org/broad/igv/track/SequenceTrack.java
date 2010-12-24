/*
 * Copyright (c) 2007-2011 by The Broad Institute, Inc. and the Massachusetts Institute of
 * Technology.  All Rights Reserved.
 *
 * This software is licensed under the terms of the GNU Lesser General Public License (LGPL),
 * Version 2.1 which is available at http://www.opensource.org/licenses/lgpl-2.1.php.
 *
 * THE SOFTWARE IS PROVIDED "AS IS." THE BROAD AND MIT MAKE NO REPRESENTATIONS OR
 * WARRANTES OF ANY KIND CONCERNING THE SOFTWARE, EXPRESS OR IMPLIED, INCLUDING,
 * WITHOUT LIMITATION, WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE, NONINFRINGEMENT, OR THE ABSENCE OF LATENT OR OTHER DEFECTS, WHETHER
 * OR NOT DISCOVERABLE.  IN NO EVENT SHALL THE BROAD OR MIT, OR THEIR RESPECTIVE
 * TRUSTEES, DIRECTORS, OFFICERS, EMPLOYEES, AND AFFILIATES BE LIABLE FOR ANY DAMAGES
 * OF ANY KIND, INCLUDING, WITHOUT LIMITATION, INCIDENTAL OR CONSEQUENTIAL DAMAGES,
 * ECONOMIC DAMAGES OR INJURY TO PROPERTY AND LOST PROFITS, REGARDLESS OF WHETHER
 * THE BROAD OR MIT SHALL BE ADVISED, SHALL HAVE OTHER REASON TO KNOW, OR IN FACT
 * SHALL KNOW OF THE POSSIBILITY OF THE FOREGOING.
 */


package org.broad.igv.track;

//~--- non-JDK imports --------------------------------------------------------

import org.broad.igv.Globals;
import org.broad.igv.feature.Strand;
import org.broad.igv.renderer.*;
import org.broad.igv.renderer.Renderer;
import org.broad.igv.ui.panel.ReferenceFrame;
import org.broad.igv.ui.panel.TrackPanelComponent;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;


/**
 * @author jrobinso
 */
public class SequenceTrack extends AbstractTrack {


    private static final int SEQUENCE_HEIGHT = 14;

    private static String PLUS_STRING = "Sequence ==>";
    private static String MINUS_STRING = "Sequence <==";

    private SequenceRenderer sequenceRenderer = new SequenceRenderer();

    //should translated aminoacids be shown below the sequence?
    private boolean shouldShowTranslation = true;

    //is sequence visible (zoomed in far enough, etc)
    private boolean sequenceVisible = false;

    Strand strand = Strand.POSITIVE;

    /**
     * If true show sequence in "color space"  (for SOLID alignments).  Currently not implemented, should always be
     * false.
     */
    private boolean showColorSpace = false;

    public SequenceTrack(String name) {
        super(name);
    }


    @Override
    public void renderName(Graphics2D graphics, Rectangle trackRectangle, Rectangle visibleRectangle) {
        if (sequenceVisible) {
            String name = strand == Strand.POSITIVE ? PLUS_STRING : MINUS_STRING;
            graphics.drawString(name, trackRectangle.x + 20, trackRectangle.y + 12);
        }
    }

    /**
     * Render the sequence, and optionally the 3 frame translation table
     *
     * @param context
     * @param rect
     */
    public void render(RenderContext context, Rectangle rect) {
        // Are we zoomed in far enough to show the sequence?  Scale is
        // in BP / pixel,  need at least 1 pixel for 3 bp in order to show translated sequence,
        //or 1 pixel per bp in order to show sequence.
        boolean visible = isSequenceVisible(context);
        if(visible != sequenceVisible) {
            sequenceVisible = visible;
            context.getPanel().invalidate();
        }
        if (sequenceVisible) {
            sequenceRenderer.setStrand(strand);
            sequenceRenderer.draw(context, rect, showColorSpace, shouldShowTranslation);
        }
    }


    private boolean isSequenceVisible(RenderContext context) {

        return context.getScale() < (shouldShowTranslation ? 3 : 1) && !context.getChr().equals(Globals.CHR_ALL);

    }

    @Override
    public int getHeight() {
        return sequenceVisible ? SEQUENCE_HEIGHT + (showColorSpace ? SEQUENCE_HEIGHT : 0) +
                (shouldShowTranslation ? SequenceRenderer.TranslatedSequenceDrawer.TOTAL_HEIGHT : 0) :
                0;
    }


    @Override
    public boolean handleDataClick(TrackClickEvent e) {

        MouseEvent evt = e.getMouseEvent();
        setShouldShowTranslation(!shouldShowTranslation);
        Object source = e.getMouseEvent().getSource();
        if (source instanceof JComponent) {
            ((JComponent) source).repaint();
        }
        return true;
    }

    /**
     * On a single-click on this track, toggle whether we show translation and redraw
     * <p/>
     * Java does not have a "double-click" event,  each click is a separate event.  To prevent action from being taken
     * on the first click of what will be a double click, the action is scheduled in the future and canceled if/when
     * the second click is handled.
     *
     * @param e
     * @return
     */
    @Override
    public void handleNameClick(final MouseEvent e) {

        strand = (strand == Strand.POSITIVE ? Strand.NEGATIVE : Strand.POSITIVE);
        Object source = e.getSource();
        if (source instanceof TrackPanelComponent) {
            ((TrackPanelComponent) source).getTrackPanel().repaint();
        }  //To change body of implemented methods use File | Settings | File Templates.

    }


    public boolean isShouldShowTranslation() {
        return shouldShowTranslation;
    }

    public void setShouldShowTranslation(boolean shouldShowTranslation) {
        this.shouldShowTranslation = shouldShowTranslation;
    }

    //----------------------------------------------------------------------------
    // Methods belowo are required for the Track interface, but aren't
    // meaningful here.  Obviously some refactoring is in order to reduce
    // the number of required methods.

    public String getValueStringAt(String chr, double position, int y, ReferenceFrame frame) {
        return null;
    }


    public void setColorScale(ContinuousColorScale colorScale) {
        // Required method for track interface, ignore
    }


    public void setStatType(WindowFunction type) {
        // Required method for track interface, ignore
    }

    public WindowFunction getWindowFunction() {
        // Required method for track interface, ignore
        return null;
    }


    public void setRendererClass(Class rc) {
        // Required method for track interface, ignore
    }


    public Renderer getRenderer() {
        // Required method for track interface, ignore
        return null;
    }


    public float getRegionScore(String chr, int start, int end, int zoom, RegionScoreType type, ReferenceFrame frame) {
        // Required method for track interface, ignore
        return 0;
    }


    public boolean isLogNormalized() {
        // Required method for track interface, ignore
        return true;
    }


}
