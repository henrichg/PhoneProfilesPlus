/*
 * This file is part of the roottools Project: http://code.google.com/p/RootTools/
 *
 * Copyright (c) 2012 Stephen Erickson, Chris Ravenscroft, Dominik Schuermann, Adam Shanks
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

package com.stericson.roottools.internal;

import com.stericson.roottools.containers.Mount;
import com.stericson.roottools.containers.Permissions;
import com.stericson.roottools.containers.Symlink;

import java.util.ArrayList;
import java.util.regex.Pattern;

class InternalVariables
{

    // ----------------------
    // # Internal Variables #
    // ----------------------


    @SuppressWarnings("WeakerAccess")
    protected static boolean nativeToolsReady = false;
    @SuppressWarnings("WeakerAccess")
    protected static boolean found = false;
    @SuppressWarnings("WeakerAccess")
    protected static boolean processRunning = false;

    @SuppressWarnings("WeakerAccess")
    protected static String[] space;
    @SuppressWarnings("WeakerAccess")
    protected static String getSpaceFor;
    @SuppressWarnings({"WeakerAccess", "unused"})
    protected static String busyboxVersion;
    @SuppressWarnings("WeakerAccess")
    protected static String pid_list = "";
    @SuppressWarnings("WeakerAccess")
    protected static ArrayList<Mount> mounts;
    @SuppressWarnings("WeakerAccess")
    protected static ArrayList<Symlink> symlinks;
    @SuppressWarnings("WeakerAccess")
    protected static String inode = "";
    @SuppressWarnings("WeakerAccess")
    protected static Permissions permissions;

    // regex to get pid out of ps line, example:
    // root 2611 0.0 0.0 19408 2104 pts/2 S 13:41 0:00 bash
    @SuppressWarnings("WeakerAccess")
    protected static final String PS_REGEX = "^\\S+\\s+([0-9]+).*$";
    @SuppressWarnings("WeakerAccess")
    protected static final Pattern psPattern;

    static
    {
        psPattern = Pattern.compile(PS_REGEX);
    }
}
