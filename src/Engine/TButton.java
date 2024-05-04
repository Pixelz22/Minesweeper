package Engine;

import Game.Game;

import java.awt.event.MouseEvent;
import java.util.ArrayList;

public class TButton {
    private ArrayList<ButtonEvent> onClickEvents = new ArrayList<>();
    public TRect rect = new TRect();

    public void addOnClickEvent(ButtonEvent event) {
        onClickEvents.add(event);
    }

    public void onClicked(MouseEvent e) {
        for (ButtonEvent event : onClickEvents) {
            Game.queuePreRenderEvent(() -> event.run(e));
        }
    }

    public interface ButtonEvent {
        void run(MouseEvent e);
    }
}
