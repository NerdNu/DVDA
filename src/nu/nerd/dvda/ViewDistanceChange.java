package nu.nerd.dvda;

import java.time.LocalTime;

// ----------------------------------------------------------------------------
/**
 * Stores the details of a scheduled view distance change.
 */
public class ViewDistanceChange implements Comparable<ViewDistanceChange> {
    // ------------------------------------------------------------------------
    /**
     * Constructor.
     *
     * @param serialised the serialised string form of the change.
     */
    ViewDistanceChange(String serialised) throws NumberFormatException {
        String[] parts = serialised.split("\\s+");
        _time = LocalTime.parse(parts[0]);
        _viewDistance = Integer.parseInt(parts[1]);
    }

    // ------------------------------------------------------------------------
    /**
     * Constructor.
     *
     * @param time the time of the change.
     * @param viewDistance the new view distance.
     */
    ViewDistanceChange(LocalTime time, int viewDistance) {
        _time = time;
        _viewDistance = viewDistance;
    }

    // ------------------------------------------------------------------------
    /**
     * Return the time of the change.
     *
     * @return the time of the change.
     */
    public LocalTime getTime() {
        return _time;
    }

    // ------------------------------------------------------------------------
    /**
     * Return the new view distance.
     *
     * @return the new view distance.
     */
    public int getViewDistance() {
        return _viewDistance;
    }

    // ------------------------------------------------------------------------
    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(ViewDistanceChange other) {
        return _time.compareTo(other._time);
    }

    // ------------------------------------------------------------------------
    /**
     * Return the serialised form of this object as a String.
     *
     * @return the serialised form.
     */
    @Override
    public String toString() {
        return _time.toString() + " " + _viewDistance;
    }

    // ------------------------------------------------------------------------
    /**
     * The time when the change is scheduled.
     */
    protected LocalTime _time;

    /**
     * New view distance.
     */
    protected int _viewDistance;
} // class ViewDistanceChange