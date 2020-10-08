package org.pangpangpi.common.utlils.tasks;

public class TaskInfo {

    private Long taskId;

    private boolean finished;

    private String taskMessage;

    private SpeedReport produceSpeed;

    private SpeedReport consumeSpeed;

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    public String getTaskMessage() {
        return taskMessage;
    }

    public void setTaskMessage(String taskMessage) {
        this.taskMessage = taskMessage;
    }

    public SpeedReport getProduceSpeed() {
        return produceSpeed;
    }

    public void setProduceSpeed(SpeedReport produceSpeed) {
        this.produceSpeed = produceSpeed;
    }

    public SpeedReport getConsumeSpeed() {
        return consumeSpeed;
    }

    public void setConsumeSpeed(SpeedReport consumeSpeed) {
        this.consumeSpeed = consumeSpeed;
    }

    public void waitForFinish() {
        Tasks.waitForFinish(getTaskId());
    }

    public void stopAndRemove() {
        Tasks.removeTask(getTaskId());
    }

    @Override
    public String toString() {
        return "TaskInfo{" +
                "taskId=" + taskId +
                ", finished=" + finished +
                ", taskMessage='" + taskMessage + '\'' +
                ", produceSpeed=" + produceSpeed +
                ", consumeSpeed=" + consumeSpeed +
                '}';
    }

}
