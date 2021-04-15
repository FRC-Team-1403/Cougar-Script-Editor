package frc.robot.autonomous;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.function.Function;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import edu.wpi.first.wpilibj.Filesystem;
import edu.wpi.first.wpilibj.geometry.Pose2d;
import edu.wpi.first.wpilibj.geometry.Rotation2d;
import edu.wpi.first.wpilibj2.command.Command;

/**
 * A utility class that helps with reading Cougar Scripts from JSON format.
 * Commands must be registered by calling the registerCommand method.
 * Scripts can be loaded using the importScript method.
 * Scripts mmust be placed directly in the deploy directory to be imported.
 * 
 * @author Brandon C.
 */
public class CougarScriptReader {

    private static CougarScriptReader instance;

    private Map<String, Function<CougarScriptObject, Command>> commandMap;

    public CougarScriptReader() {
        if (instance != null) {return;}
        instance = this;
        
        //register parallelcommand automatically
        commandMap = new HashMap<String, Function<CougarScriptObject, Command>>();
        registerCommand("ParallelCommand", (CougarScriptObject p) -> {
            ArrayList<Command> commandsToRun = new ArrayList<>();
            ArrayList<Command> endCommands = new ArrayList<Command>();
            JSONArray commandListJSON = p.getJSONArray("Commands");
            for (int i = 0; i < commandListJSON.length(); i++) {
                JSONObject parallelFieldJSON = commandListJSON.getJSONObject(i);
                boolean endCondition = parallelFieldJSON.getBoolean("EndCondition");
                Command currentCommand = parseCommandFromJSON(parallelFieldJSON.getJSONObject("Command"));
                if (currentCommand != null) {
                    commandsToRun.add(currentCommand);
                    if (endCondition) {
                        endCommands.add(currentCommand);
                    }
                }
            }
            return new ParallelCommand(commandsToRun, endCommands);
        });
    }

    /**
     * Creates a sequential command group from the cougar script JSON file.
     * @param autoName The filename of the script, located in the deploy directory
     * NOTE: do not create directories in the deploy directory.
     * @return Sequential command group containing all the commands from the script.
     */
    public SequentialCommandGroup importScript(String autoName) {
        String filepath = Filesystem.getDeployDirectory()+"/"+autoName;
        ArrayList<Command> commands = new ArrayList<Command>();
        Pose2d startPose = new Pose2d();
        File fileToOpen = new File(filepath);
        try (Scanner sc = new Scanner(fileToOpen);) {
            String data = "";
            while (sc.hasNextLine()) {
                data += sc.nextLine();
            }
            sc.close();
            JSONTokener tokener = new JSONTokener(data);
            JSONArray commandList = new JSONArray(tokener);
            for (int i = 0; i < commandList.length(); i++) {
                JSONObject commandJSON = commandList.getJSONObject(i);
                if (((String)commandJSON.get("CommandName")).equals("SetStartPosition")) {
                    JSONObject parameters = (JSONObject) commandJSON.get("Parameters");
                    startPose = new Pose2d(
                        parameters.getDouble("x"),
                        parameters.getDouble("y"),
                        new Rotation2d(parameters.getDouble("angle"))
                    );
                } 
                Command command = parseCommandFromJSON(commandJSON);
                if (command != null) {
                    commands.add(command);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new SequentialCommandGroup(startPose, commands.toArray(new Command[commands.size()]));
    }

    private Command parseCommandFromJSON(JSONObject commandJSON) {
        String commandName = (String) commandJSON.get("CommandName");
        if (commandName.equals("SetStartPosition")) {
            return null;
        }
        JSONObject parameters = (JSONObject) commandJSON.get("Parameters");
        Function<CougarScriptObject, Command> f = commandMap.get(commandName);
        if (f != null) {
            return f.apply(new CougarScriptObject(parameters));
        } else {
            return null;
        }
    }

    /**
     * Registers a command to be able to create it from the Cougar Script.
     * @param commandName The name of the command, matching on the Script Editor exactly (case sensitive)
     * @param f The function that determines how to create the command based on the info in the Cougar Script.
     * The function takes a CougarScriptObject and returns a Command. The Cougar script object enables
     * access of the different parameters stored in JSON for that command, and the Function will return
     * a command. The parameters for the command can be accessed through the CougarScriptObject and 
     * these values can be used to specify how to create the command in the function. These parameters
     * should match with the ones specified in the Script Editor (case sensitive).
     * 
     * <p>Example:
     * <p>In the script editor, there is a robotCommand called "myDrive" which has a double parameter 
     * "Distance" and a boolean parameter "Direction" (forwards or backwards).
     * 
     * <p>In the robot code, there is a command called "ExampleDriveCommand" with the following constructor:
     * public ExampleDriveCommand(Subsystem driveSubsystem, double distance, boolean direction)
     * 
     * <p>Here's an example of how this command could be registered:
     * <p>registerCommand("myDrive", (CougarScriptObject parameters) -> {
     * <p>     Subsystem driveSubsystem = robotContainer.getDriveSubsystem();
     * <p>     double distance = parameters.getDouble("Distance");
     * <p>     boolean direction = parameters.getBoolean("Direction");
     * <p>     Command output = new ExampleDriveCommand(driveSubsystem, distance, direction);
     * <p>     return output;
     * <p>});
     * 
     * Note that the lambda expression takes a CougarScriptObject and returns a Command.
     * 
     */
    public void registerCommand(String commandName, Function<CougarScriptObject, Command> f) {
        commandMap.put(commandName, f);
    }
}