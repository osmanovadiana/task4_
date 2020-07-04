package ru.vsu.cs.course1;

import java.util.ArrayList;
import java.util.List;

public class SortStateManager {
    private List<SortState> states;
    private int current = 0;

    private boolean flag = false;
    public boolean isFlag() { return flag; }
    public void setFlag(boolean flag) { this.flag = flag; }

    public SortStateManager(){ this(null); }
    public SortStateManager(List<SortState> list){ setStates(list); }

    public void setStates(List<SortState> states) {
        this.states = new ArrayList<>();
        if(states != null)
            this.states.addAll(states);
        reset();
    }

    public interface SortStateChangedListener{
        void stateChanged(SortState state, int index, int total);
        void finished();
    }

    private SortStateChangedListener listener = null;

    public void setListener(SortStateChangedListener listener){ this.listener = listener; }

    protected void onSortStateChanged(){
        if (listener == null)
            return;
        if(states.size() == 0)
            listener.stateChanged(null, -1, 0);
        else
            listener.stateChanged(states.get(current), current, states.size());
    }

    protected void onFinished(){
        if (listener != null)
            listener.finished();
    }

    public void reset(){
        current = 0;
        onSortStateChanged();
    }

    public void setCurrentIndex(int value) {
        current = value;
    }

    public int getCurrentIndex() { return current; }
    public int getTotalStateAmount(){ return states.size(); }

    private SortState getCurrentState(){
        return states.size() == 0 ? null : states.get(current);
    }

    public boolean next(){
        if(current < states.size() - 1)
            current++;
        else if (flag)
            current = 0;
        else return false;
        onSortStateChanged();
        if(current == states.size() - 1)
            onFinished();
        return true;
    }

    public boolean prev(){
        if (current > 0)
            current--;
        else if(flag)
            current = states.size() - 1;
        else
            return false;
        onSortStateChanged();
        return true;
    }

}
