/*
 * This file is part of the rootshell Project: http://code.google.com/p/RootShell/
 *
 * Copyright (c) 2014 Stephen Erickson, Chris Ravenscroft
 *
 * This code is dual-licensed under the terms of the Apache License Version 2.0 and
 * the terms of the General Public License (GPL) Version 2.
 * You may use this code according to either of these licenses as is most appropriate
 * for your project on a case-by-case basis.
 *
 * The terms of each license can be found in the root directory of this project's repository as well as at:
 *
 * * http://www.apache.org/licenses/LICENSE-2.0
 * * http://www.gnu.org/licenses/gpl-2.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under these Licenses is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See each License for the specific language governing permissions and
 * limitations under that License.
 */

package com.stericson.rootshell.execution;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.stericson.rootshell.RootShell;

import java.io.IOException;

public class Command {

    //directly modified by JavaCommand
    protected boolean javaCommand = false;
    protected Context context = null;

    public int totalOutput = 0;

    public int totalOutputProcessed = 0;

    ExecutionMonitor executionMonitor = null;

    Handler mHandler = null;

    //Has this command already been used?
    protected boolean used = false;

    boolean executing = false;

    final String[] command;

    boolean finished = false;

    boolean terminated = false;

    boolean handlerEnabled = true;

    int exitCode = -1;

    final int id;

    int timeout = RootShell.defaultCommandTimeout;

    /**
     * Constructor for executing a normal shell command
     *
     * @param id      the id of the command being executed
     * @param command the command, or commands, to be executed.
     */
    public Command(int id, String... command) {
        this.command = command;
        this.id = id;

        createHandler(RootShell.handlerEnabled);
    }

    /**
     * Constructor for executing a normal shell command
     *
     * @param id             the id of the command being executed
     * @param handlerEnabled when true the handler will be used to call the
     *                       callback methods if possible.
     * @param command        the command, or commands, to be executed.
     */
    public Command(int id, boolean handlerEnabled, String... command) {
        this.command = command;
        this.id = id;

        createHandler(handlerEnabled);
    }

    /**
     * Constructor for executing a normal shell command
     *
     * @param id      the id of the command being executed
     * @param timeout the time allowed before the shell will give up executing the command
     *                and throw a TimeoutException.
     * @param command the command, or commands, to be executed.
     */
    public Command(int id, int timeout, String... command) {
        this.command = command;
        this.id = id;
        this.timeout = timeout;

        createHandler(RootShell.handlerEnabled);
    }

    //If you override this you MUST make a final call
    //to the super method. The super call should be the last line of this method.
    public void commandOutput(int id, String line) {
        RootShell.log("Command", "ID: " + id + ", " + line);
        totalOutputProcessed++;
    }

    @SuppressWarnings({"unused", "EmptyMethod"})
    public void commandTerminated(int id, String reason) {
        //pass
    }

    @SuppressWarnings({"unused", "EmptyMethod"})
    public void commandCompleted(int id, int exitCode) {
        //pass
    }

    protected final void commandFinished() {
        if (!terminated) {
            synchronized (this) {
                if (mHandler != null && handlerEnabled) {
                    Message msg = mHandler.obtainMessage();
                    Bundle bundle = new Bundle();
                    bundle.putInt(CommandHandler.ACTION, CommandHandler.COMMAND_COMPLETED);
                    msg.setData(bundle);
                    mHandler.sendMessage(msg);
                } else {
                    commandCompleted(id, exitCode);
                }

                RootShell.log("Command " + id + " finished.");
                finishCommand();
            }
        }
    }

    private void createHandler(boolean handlerEnabled) {

        this.handlerEnabled = handlerEnabled;

        if (Looper.myLooper() != null && handlerEnabled) {
            RootShell.log("CommandHandler created");
            mHandler = new CommandHandler(null);
        } else {
            RootShell.log("CommandHandler not created");
        }
    }

    @SuppressWarnings("unused")
    public final void finish()
    {
        RootShell.log("Command finished at users request!");
        commandFinished();
    }

    protected final void finishCommand() {
        this.executing = false;
        this.finished = true;
        this.notifyAll();
    }


    public final String getCommand() {
        StringBuilder sb = new StringBuilder();

        if(javaCommand) {
            String filePath = context.getFilesDir().getPath();

            for (String aCommand : command) {
                /*
                 * Make withFramework optional for applications
                 * that do not require access to the fw. -CFR
                 */
                //export CLASSPATH=/data/user/0/ch.masshardt.emailnotification/files/anbuild.dex ; app_process /system/bin
                //if (Build.VERSION.SDK_INT > 22) {
                    //dalvikvm command is not working in Android Marshmallow
                    //noinspection StringConcatenationInsideStringBufferAppend
                    sb.append(
                            "export CLASSPATH=" + filePath + "/anbuild.dex;"
                                    + " app_process /system/bin "
                                    + aCommand);
                /*} else {
                    //noinspection StringConcatenationInsideStringBufferAppend
                    sb.append(
                            "dalvikvm -cp " + filePath + "/anbuild.dex"
                                    + " com.android.internal.util.WithFramework"
                                    + " com.stericson.roottools.containers.RootClass "
                                    + aCommand);
                }*/

                sb.append('\n');
            }
        }
        else {
            for (String aCommand : command) {
                sb.append(aCommand);
                sb.append('\n');
            }
        }

        return sb.toString();
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public final boolean isExecuting() {
        return executing;
    }

    @SuppressWarnings("unused")
    public final boolean isHandlerEnabled() {
        return handlerEnabled;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public final boolean isFinished() {
        return finished;
    }

    public final int getExitCode() {
        return this.exitCode;
    }

    protected final void setExitCode(int code) {
        synchronized (this) {
            exitCode = code;
        }
    }

    protected final void startExecution() {
        this.used = true;
        executionMonitor = new ExecutionMonitor(this);
        executionMonitor.setPriority(Thread.MIN_PRIORITY);
        executionMonitor.start();
        executing = true;
    }

    @SuppressWarnings("unused")
    public final void terminate()
    {
        RootShell.log("Terminating command at users request!");
        terminated("Terminated at users request!");
    }

    protected final void terminate(@SuppressWarnings("SameParameterValue") String reason) {
        try {
            Shell.closeAll();
            RootShell.log("Terminating all shells.");
            terminated(reason);
        } catch (IOException ignored) {}
    }

    protected final void terminated(String reason) {
        synchronized (Command.this) {

            if (mHandler != null && handlerEnabled) {
                Message msg = mHandler.obtainMessage();
                Bundle bundle = new Bundle();
                bundle.putInt(CommandHandler.ACTION, CommandHandler.COMMAND_TERMINATED);
                bundle.putString(CommandHandler.TEXT, reason);
                msg.setData(bundle);
                mHandler.sendMessage(msg);
            } else {
                commandTerminated(id, reason);
            }

            RootShell.log("Command " + id + " did not finish because it was terminated. Termination reason: " + reason);
            setExitCode(-1);
            terminated = true;
            finishCommand();
        }
    }

    protected final void output(int id, String line) {
        totalOutput++;

        if (mHandler != null && handlerEnabled) {
            Message msg = mHandler.obtainMessage();
            Bundle bundle = new Bundle();
            bundle.putInt(CommandHandler.ACTION, CommandHandler.COMMAND_OUTPUT);
            bundle.putString(CommandHandler.TEXT, line);
            msg.setData(bundle);
            mHandler.sendMessage(msg);
        } else {
            commandOutput(id, line);
        }
    }

    private class ExecutionMonitor extends Thread {

        private final Command command;

        ExecutionMonitor(Command command) {
            this.command = command;
        }

        public void run() {

            if(command.timeout > 0)
            {
                synchronized (command) {
                    try {
                        RootShell.log("Command " + command.id + " is waiting for: " + command.timeout);
                        command.wait(command.timeout);
                    } catch (InterruptedException e) {
                        RootShell.log("Exception: " + e);
                    }

                    if (!command.isFinished()) {
                        RootShell.log("Timeout Exception has occurred for command: " + command.id + ".");
                        terminate("Timeout Exception");
                    }
                }
            }
        }
    }

    @SuppressLint("HandlerLeak")
    private class CommandHandler extends Handler {

        static final String ACTION = "action";

        static final String TEXT = "text";

        static final int COMMAND_OUTPUT = 0x01;

        static final int COMMAND_COMPLETED = 0x02;

        static final int COMMAND_TERMINATED = 0x03;

        @SuppressWarnings({"deprecation", "unused"})
        CommandHandler(Looper looper) {
        }

        public final void handleMessage(Message msg) {
            int action = msg.getData().getInt(ACTION);
            String text = msg.getData().getString(TEXT);

            switch (action) {
                case COMMAND_OUTPUT:
                    commandOutput(id, text);
                    break;
                case COMMAND_COMPLETED:
                    commandCompleted(id, exitCode);
                    break;
                case COMMAND_TERMINATED:
                    commandTerminated(id, text);
                    break;
            }
        }
    }

}
