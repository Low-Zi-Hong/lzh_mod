package lzh.net.feature.botSystem;

import java.util.Queue;

public class WaitToReact {
    public static void waitAwhile(Queue<autoBot.ActionSubunit> subtask) {
        int globalTimer = autoBot.GlobalTick;
        subtask.add(new autoBot.ActionSubunit(globalTimer+20,() -> {
            //do nothing...
            return;
        }));
    }
}
