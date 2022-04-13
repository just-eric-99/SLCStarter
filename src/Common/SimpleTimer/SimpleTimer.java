package Common.SimpleTimer;

public class SimpleTimer extends Thread {
    private int millis;
    private PeriodAction periodAction;
    private WakeUpAction wakeUpAction;
    private WakeUpCondition condition;
    private boolean isRepeat;
    private Integer repeatTimes;

    public SimpleTimer(int second) {
        millis = second * 1000;
        periodAction = () -> {};
        wakeUpAction = () -> {};
        condition = () -> false;
        isRepeat = false;
        repeatTimes = null;
    }

    @Override
    public void run() {
        try {
            do {
                periodAction.process();
                for (int i = 0; !condition.isWake() && i < millis / 500; i++) {
                    Thread.sleep(500);
                }
                if (isRepeat && repeatTimes != null && --repeatTimes <= 0)
                    break;
            } while (!condition.isWake() && isRepeat);
            wakeUpAction.wakeAction();
        } catch (InterruptedException e) {}
    }

    public SimpleTimer periodAction(PeriodAction periodAction) {
        this.periodAction = periodAction;
        return this;
    }

    public SimpleTimer wakeIf(WakeUpCondition condition) {
        this.condition = condition;
        return this;
    }

    public SimpleTimer afterWake(WakeUpAction wakeUpAction) {
        this.wakeUpAction = wakeUpAction;
        return this;
    }

    public SimpleTimer setTime(int second) {
        millis = second * 1000;
        return this;
    }

    public SimpleTimer isRepeat(boolean repeat) {
        isRepeat = repeat;
        return this;
    }

    public SimpleTimer doFor(int times) {
        repeatTimes = times;
        return this;
    }
}
