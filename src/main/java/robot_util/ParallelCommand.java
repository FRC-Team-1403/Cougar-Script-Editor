package frc.robot.autonomous;

import java.util.ArrayList;

import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.CommandBase;

public class ParallelCommand extends CommandBase {
    
    private final ArrayList<Command> commandsToRun;
    private final ArrayList<Command> endCommands;

    public ParallelCommand(ArrayList<Command> commandsToRun, ArrayList<Command> endCommands) {
        this.commandsToRun = commandsToRun;
        this.endCommands = endCommands;
    }

    @Override
    public void initialize() {
        for (Command i: commandsToRun) {
            i.initialize();
        }
    }
    
    @Override
    public void execute() {
        for (Command i: commandsToRun) {
            System.out.println(i.getClass());
            i.execute();
        }
    }
  
    @Override
    public void schedule() {
        for(Command c : commandsToRun) {
            c.schedule();
        }
    }

    @Override
    public void end(boolean interrupted) {
        for(Command c : commandsToRun) {
            c.cancel();
        }
    }

    @Override
    public boolean isFinished() {
        for (Command i: endCommands) {
            if(!i.isFinished())
                return false;
        }
        return true;
    }
}