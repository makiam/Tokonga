package artofillusion.animation;

import artofillusion.object.ObjectInfo;

public class DummyParentedWeightedTrack extends DummyTrack {
    private ObjectInfo owner;
    private final WeightTrack weightTrack = new WeightTrack(this);

    DummyParentedWeightedTrack(ObjectInfo owner) {
        this.owner = owner;
    }

    @Override
    public Object getParent() {
        return owner;
    }

    public WeightTrack getWeightTrack() {
        return weightTrack;
    }
}
