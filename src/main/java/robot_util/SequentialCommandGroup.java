package frc.robot.autonomous;

import java.util.ArrayList;

import edu.wpi.first.wpilibj.geometry.Pose2d;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.CommandBase;
import frc.robot.subsystems.SubsystemManager;


public class SequentialCommandGroup extends CommandBase {
    public ArrayList<Command> commandsToRun;
    private int currIndex = 0;
    private int maxIndex = 0;
    private Command toRun;
    private boolean isFinished = false;
    private Pose2d startPose;

    public SequentialCommandGroup(Pose2d startPose, Command... c_iter) {
        this.commandsToRun = new ArrayList<>(); 
        for (Command i: c_iter) {
            this.commandsToRun.add(i);
        }
        this.maxIndex = this.commandsToRun.size();
        this.toRun = this.commandsToRun.get(this.currIndex);
        this.startPose = startPose;
    }

    public SequentialCommandGroup(Command... c_iter) {
        this(null, c_iter);
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

    public void remove() {
        // TODO later; idk if this is necessary
    }

    // Only called like this so you know to put it in the main teleop loop
    @Override
    public void schedule() {
        super.schedule();
        this.toRun.schedule();
        if (startPose != null) {
            SubsystemManager.getInstance().getNEODrivetrain().poseEstimator.setPose(startPose);
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