
package org.twak.siteplan.anchors;

import javax.swing.JComponent;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import org.twak.camp.Corner;
import org.twak.camp.Edge;
import org.twak.camp.SkeletonCapUpdate;
import org.twak.camp.debug.DebugDevice;
import org.twak.camp.ui.Marker;
import org.twak.siteplan.anchors.AnchorHauler.AnchorHeightEvent;
import org.twak.siteplan.campskeleton.Siteplan;
import org.twak.siteplan.campskeleton.Plan;
import org.twak.siteplan.campskeleton.PlanSkeleton;
import org.twak.utils.LContext;
import org.twak.utils.WeakListener;
import org.twak.utils.WeakListener.Changed;
import org.twak.utils.collections.DHash;
import org.twak.utils.collections.LoopL;
import org.twak.utils.collections.Loopable;
import org.twak.utils.collections.SetCorrespondence;

/**
 *
 * @author twak
 */
public class CapShip extends Ship
{
    transient boolean addCap = true; // really for debug no need to serialize yet
    boolean atZeroHeight = false;

    @Override
    public JComponent getToolInterface( WeakListener refreshAnchors, Changed refreshFeatureListListener, Plan plan )
    {
        return new CapShipUI( this, plan );
    }

    @Override
    protected Instance createInstance()
    {
        return new CapInstance();
    }

    public class CapInstance extends Instance
    {
        @Override
        public LContext<Corner> process( Anchor anchor, LContext<Corner> toEdit, Marker planMarker, Marker profileMarker, Edge edge, AnchorHeightEvent hauler, Corner oldLeadingCorner )
        {
            PlanSkeleton skel = hauler.skel;

            SkeletonCapUpdate capUpdate = new SkeletonCapUpdate(skel);

            LoopL<Corner> flatTop = capUpdate.getCap(-profileMarker.y);

            capUpdate.update(new LoopL(), new SetCorrespondence<Corner, Corner>(), new DHash<Corner, Corner>());

            if (addCap)
            {
                LoopL<Point3d> togo =
                flatTop.new Map<Point3d>()
                {
                    @Override
                    public Point3d map( Loopable<Corner> input )
                    {
                        return new Point3d( input.get().x, input.get().y, atZeroHeight ? 0.01 :  input.get().z );
                    }
                }.run();
                skel.output.addNonSkeletonOutputFace2( togo, new Vector3d( 0, 0, 1 ) );
            }



            skel.qu.clearFaceEvents();
            skel.qu.clearOtherEvents();

            DebugDevice.dump("post cap dump", skel);
            
            return null;
        }
    }

    @Override
    protected Anchor createNewAnchor()
    {
        return Siteplan.instance.plan.createAnchor( Plan.AnchorType.PROFILE, this );
    }

    @Override
    public String getFeatureName()
    {
        return ("cap");
    }

    @Override
    public Ship clone( Plan plan )
    {
        return new CapShip();
    }

}
