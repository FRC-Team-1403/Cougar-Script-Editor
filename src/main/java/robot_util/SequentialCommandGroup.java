package frc.robot;

import java.util.ArrayList;
import java.util.function.Consumer;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.CommandBase;


public class SequentialCommandGroup extends CommandBase {
    public ArrayList<Command> commandsToRun;
    private int currIndex = 0;
    private int maxIndex = 0;
    private Command toRun;
    private boolean isFinished = false;
    private Pose2d startPose;
    private Consumer<Pose2d> onStart;

    public SequentialCommandGroup(Pose2d startPose, Consumer<Pose2d> onStart, Command... c_iter) {
        this.commandsToRun = new ArrayList<>(); 
        for (Command i: c_iter) {
            this.commandsToRun.add(i);
        }
        this.maxIndex = this.commandsToRun.size();
        this.toRun = this.commandsToRun.get(this.currIndex);
        this.startPose = startPose;
        this.onStart = onStart;
    }

    public SequentialCommandGroup(Command... c_iter) {
        this(null, null, c_iter);
    }

    public void setStartPose(Pose2d pose) {
        this.startPose = pose;
    }

    public void add(Command command) {
        this.commandsToRun.add(command);
        this.maxIndex++;
    }

    public void reset() {
        this.currIndex = 0;
        this.toRun = this.commandsToRun.get(this.currIndex);
        isFinished = false;
    }

    // Only called like this so you know to put it in the main teleop loop
    @Override
    public void schedule() {
        super.schedule();
        this.toRun.schedule();
        if (startPose != null) {
            onStart.accept(startPose);
        }
        reset();
    }

    @Override
    public void execute() {
        if(toRun.isFinished()) {
            toRun.end(false);      
            currIndex++;
            if(currIndex == maxIndex){
                isFinished = true;
                return;
            }
            toRun = commandsToRun.get(currIndex);
            toRun.schedule();
        }
    }

    @Override
    public void end(boolean interrupted) {
        reset();
    }

    @Override
    public boolean isFinished() {
        return isFinished;
    }
}