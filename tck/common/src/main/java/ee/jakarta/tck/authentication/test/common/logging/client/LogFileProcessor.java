/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation.
 * Copyright (c) 2007, 2020 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package ee.jakarta.tck.authentication.test.common.logging.client;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.Files;
import java.util.Collection;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ee.jakarta.tck.authentication.test.common.TSLogging;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * LogFileProcessor does the following operations
 *
 * 1) Fetches log records from authentication-trace-log.xml
 *
 * 2) Checks for the existence of search string in the log for example to verify whether server log contains a string
 * "Java EE rocks" use the following code
 * <code>
 * LogFileProcessor logProcessor = new LogFileProcessor(...);
 * boolean contains = logProcessor.verifyLogContains("Jakarta EE rocks");
 * </code>
 *
 * 3) Prints the collection of log records.
 *
 * @author Raja Perumal
 */
public class LogFileProcessor {

    private Collection recordCollection;
    private Collection appIdRecordCollection;
    private Collection appSpecificRecordCollection;

    private boolean client;

    public LogFileProcessor() {
        this(false);
    }

    public LogFileProcessor(boolean client) {
        this.client = client;
    }

    /**
     * FetchLogs pull logs from the server.
     *
     */
    public void fetchLogs() {

        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

            final File logFile = client ? TSLogging.FILE_TEST : TSLogging.FILE_WEBAPP;
            if (logFile == null || !logFile.exists()) {
                System.out.println("Check permissions for log file ");
                System.out.println("See User guide for Configuring log file permissions");
                throw new Error("Log File " + logFile + " not set or does not exist");
            }
            // Sometimes we parse unfinished log files
            String content = Files.readString(logFile.toPath(), UTF_8).trim();
            String endLogTag = "</log>";
            if (!content.endsWith(endLogTag)) {
                content += endLogTag;
            }
            final NodeList nodes;
            try (ByteArrayInputStream input = new ByteArrayInputStream(content.getBytes(UTF_8))) {
                Document document = documentBuilder.parse(input);
                Element rootElement = document.getDocumentElement();
                nodes = rootElement.getChildNodes();
            }
            recordCollection = pullAllLogRecords("fullLog", nodes);
        } catch (Exception e) {
            throw new Error("Error in fetchLogs()", e);
        }
    }

    /**
     * Fetches all JSR196 SPI logs from authentication-trace-log.xml
     */
    public static Collection pullAllLogRecords(String queryParams, NodeList nodes) throws Exception {
        Node childNode;
        Node recordNode;
        NodeList recordNodeChildren;
        Collection recordCollection = new Vector();

        for (int i = 0; i < nodes.getLength(); i++) {
            // Take the first record
            recordNode = nodes.item(i);

            if (recordNode.getNodeName().equals("record")) {
                LogRecordEntry recordEntry = new LogRecordEntry(recordNode);
                recordCollection.add(recordEntry);

            }
        }
        return recordCollection;
    }

    public void setAppIdRecordCollection(Collection recordCollection) {
        this.appIdRecordCollection = recordCollection;
    }

    public Collection getAppIdRecordCollection() {
        return this.appIdRecordCollection;
    }

    public void setRecordCollection(Collection recordCollection) {
        this.recordCollection = recordCollection;
    }

    public Collection getRecordCollection() {
        return this.recordCollection;
    }

    public void setAppSpecificRecordCollection(Collection recordCollection) {
        this.appSpecificRecordCollection = recordCollection;
    }

    public Collection getAppSpecificRecordCollection() {
        return this.appSpecificRecordCollection;
    }

    /**
     * Checks for the existance of search string in the log. For example to verify whether server log contains a string
     * "Jakarta EE rocks" use the following code
     *
     * LogFileProcessor logProcessor = new LogFileProcessor(...);
     * boolean contains = logProcessor.verifyLogContains("Jakarta EE rocks");
     */
    public boolean verifyLogContains(String... args) {
        return verifyLogContains(false, args);
    }

    /**
     * Checks for the existance of search string in the log. For example to verify whether server log contains a string
     * "Java EE rocks" use the following code
     *
     * LogFileProcessor logProcessor = new LogFileProcessor(...);
     * boolean contains = logProcessor.verifyLogContains("Jakarta EE rocks");
     */
    public boolean verifyLogContains(boolean validateOrder, String... args) {
        LogRecordEntry recordEntry = null;
        TestUtil.logMsg("Searching log records for record :" + args[0]);
        if (recordCollection == null) {
            TestUtil.logMsg("Record collection empty : No log records found");
            return false;
        }

        TestUtil.logMsg("Record collection has:  " + recordCollection.size() + " records.");

        int numberOfArgs = args.length;
        int numberOfMatches = 0;

        boolean argsMatchIndex[] = new boolean[args.length];
        for (int i = 0; i < args.length; i++) {
            // initialize all argsMatchIndex to "false" (i.e no match)
            argsMatchIndex[i] = false;

            // From the given string array(args) if there is a record match
            // for the search string, then the corresponding argsMatchIndex[i]
            // will be set to true(to indicate a match)
            // i.e argsMatchIndex[i] = true;
            //
            // For example if the string array contains
            // String args[]={"JK", "EMERSON", "J.B.Shaw};
            //
            // And if the string "JK" and "J.B.Shaw" are found in the records
            // then the argsMatchIndex will be set as shown below
            // argsMatchIndex[] ={true, false, true};
            //
        }

        Iterator iterator = recordCollection.iterator();
        if (!validateOrder) {
            // we dont care about order, just that all args were found
            while (iterator.hasNext()) {
                // loop thru all message tag/entries in the log file
                recordEntry = (LogRecordEntry) iterator.next();
                String message = recordEntry.getMessage();
                // loop through all arguments to search for a match
                for (int i = 0; i < numberOfArgs; i++) {

                    // Search only unique record matches ignore repeat occurances
                    if (argsMatchIndex[i] != true) {
                        // see if one of the search argument matches with
                        // the logfile message entry and if so return true
                        if ((message != null) && message.equals(args[i])) {
                            TestUtil.logMsg("Matching Record :");
                            TestUtil.logMsg(recordEntry.getMessage());

                            // Increment match count
                            numberOfMatches++;

                            // Mark the matches in argsMatchIndex
                            argsMatchIndex[i] = true;

                            continue;
                        }
                    }
                }
                // Return true if, we found matches for all strings
                // in the given string array
                if (numberOfMatches == numberOfArgs) {
                    return true;
                }
            }

            // if here, our non-order-specific search did not find all matches...
            // Print unmatched Strings(i.e no matches were found for these strings)
            TestUtil.logMsg("No Matching log Record(s) found for the following String(s) :");
            for (int i = 0; i < numberOfArgs; i++) {
                if (argsMatchIndex[i] == false) {
                    TestUtil.logMsg(args[i]);
                }
            }

        } else {
            // we care about order, so find args and make sure they are in
            // same order as the arg[] array
            int recordCnt = 0;
            int argCount = 0;

            // first find how last occurance of our FIRST ITEM. Why? because
            // we want to be sure that if we are searching for "a", "b", "c" within
            // the authentication-trace-log.xml, we better find the very last and most recent entry
            // for "a". For example, if logfile contain "a", "b", "c", "a", "c" due
            // to multiple runs, we will erroneously pass a search for "a", "b", "c"
            // UNLESS we are careful to find the last "a", then do our check.

            int occurances = 0;
            while (iterator.hasNext()) {
                recordCnt++;
                recordEntry = (LogRecordEntry) iterator.next();
                String message = recordEntry.getMessage();
                if ((message != null) && message.equals(args[argCount])) {
                    occurances++;
                    TestUtil.logMsg("verifyLogContains() found " + occurances + "occurances");
                }
            }

            // reset the iterator
            iterator = recordCollection.iterator();
            while (iterator.hasNext()) {
                recordCnt++;
                recordEntry = (LogRecordEntry) iterator.next();
                String message = recordEntry.getMessage();

                if ((message != null) && (message.equals(args[0])) && (occurances > 1)) {
                    // found a match but not the LAST occurance of it in authentication-trace-log.xml
                    // so keep looking
                    occurances--;
                    continue;
                }

                if ((message != null) && message.equals(args[argCount])) {
                    // we found last occurance of first item in args[]
                    TestUtil.logMsg("Found: " + args[argCount] + " in record entry: " + recordCnt);
                    argCount++;
                    if (argCount >= args.length) {
                        // we are done, found all our matches in order
                        return true;
                    }
                }
            }

            // if here, our order-specific-search did not succeed.
            TestUtil.logMsg("Failed to find order-specific-records search in verifyLogContains()");
            TestUtil.logMsg("   Could not find: " + args[argCount]);
        }

        return false;
    }

    /**
     * Checks for the existance of one of the search string(from a given String array.
     *
     * For example to verify whether server log contains one of the following String String[] arr ={"aaa", "bbb", "ccc"};
     *
     * LogFileProcessor logProcessor = new LogFileProcessor(...); boolean contains =
     * logProcessor.verifyLogContainsOneOf(arr);
     *
     * This method will return true if the log file contains one of the specified String (say "aaa" )
     */
    public boolean verifyLogContainsOneOf(String args[]) {
        LogRecordEntry recordEntry = null;
        boolean result = false;

        TestUtil.logMsg("Searching log records for the presence of one of the String" + " from a given string array");
        if (recordCollection == null) {
            TestUtil.logMsg("Record collection empty : No log records found");
            return false;
        } else {
            TestUtil.logMsg("Record collection has:  " + recordCollection.size() + " records.");
        }

        int numberOfArgs = args.length;

        Iterator iterator = recordCollection.iterator();
        searchLabel: while (iterator.hasNext()) {
            // loop thru all message tag/entries in the log file
            recordEntry = (LogRecordEntry) iterator.next();
            String message = recordEntry.getMessage();
            // loop through all arguments to search for a match
            for (int i = 0; i < numberOfArgs; i++) {

                // see if one of the search argument matches with
                // the logfile message entry and if so return true
                if ((message != null) && message.equals(args[i])) {
                    TestUtil.logMsg("Matching Record :");
                    TestUtil.logMsg(recordEntry.getMessage());
                    result = true;

                    // If a match is found no need to search further
                    break searchLabel;
                }
            }

        }

        if (!result) {
            // Print unmatched Strings(i.e no matches were found for these strings)
            TestUtil.logMsg("No Matching log Record(s) found for the following String(s) :");
            for (int i = 0; i < numberOfArgs; i++) {
                TestUtil.logMsg(args[i]);
            }
        }

        return result;
    }

    /**
     * This method looks for the presence of the given substring (from the array of strings "args") in the serverlog, which
     * starts with the given "srchStrPrefix" search-string-prefix.
     *
     *
     * For example to verify whether server log contains one of the following Strings in a server log with appContextId as
     * the message prefix we can issue the following command
     *
     * String[] arr ={"aaa", "bbb", "ccc"}; String srchStrPrefix ="appContextId";
     *
     * LogFileProcessor logProcessor = new LogFileProcessor(...);
     * boolean contains = logProcessor.verifyLogContainsOneOf(arr);
     *
     * "appContextId= xxxx aaa yyyyyyyyyyyyyyyyy" "appContextId= yyyy bbb xxxxxxxxxxxxxxxxx"
     *
     * This method will return true if the log file contains one of the specified String (say "aaa" ) in the message log
     * with "appContextId" as its message prefix.
     */
    public boolean verifyLogContainsOneOfSubString(String args[], String srchStrPrefix) {
        LogRecordEntry recordEntry = null;
        boolean result = false;

        TestUtil.logMsg("Searching log records for the presence of one of the String" + " from a given string array");
        if (recordCollection == null) {
            TestUtil.logMsg("Record collection empty : No log records found");
            return false;
        } else {
            TestUtil.logMsg("Record collection has:  " + recordCollection.size() + " records.");
        }

        int numberOfArgs = args.length;

        Iterator iterator = recordCollection.iterator();
        searchLabel: while (iterator.hasNext()) {
            // loop thru all message tag/entries in the log file
            recordEntry = (LogRecordEntry) iterator.next();
            String message = recordEntry.getMessage();
            // loop through all arguments to search for a match
            for (int i = 0; i < numberOfArgs; i++) {

                // see if one of the search argument matches with
                // the logfile message entry and if so return true
                if ((message != null) && (message.startsWith(srchStrPrefix, 0)) && (message.indexOf(args[i]) > 0)) {
                    TestUtil.logMsg("Matching Record :");
                    TestUtil.logMsg(recordEntry.getMessage());
                    result = true;

                    // If a match is found no need to search further
                    break searchLabel;
                }
            }

        }

        if (!result) {
            // Print unmatched Strings(i.e no matches were found for these strings)
            TestUtil.logMsg("No Matching log Record(s) found for the following String(s) :");
            for (int i = 0; i < numberOfArgs; i++) {
                TestUtil.logMsg(args[i]);
            }
        }

        return result;
    }

    public void printCollection(Collection recordCollection) {
        LogRecordEntry recordEntry = null;
        Iterator iterator = recordCollection.iterator();

        while (iterator.hasNext()) {
            recordEntry = (LogRecordEntry) iterator.next();
            printRecordEntry(recordEntry);
        }
    }

    public void printRecordEntry(LogRecordEntry rec) {
        TestUtil.logMsg("*******Log Content*******");

        TestUtil.logMsg("Milli Seconds  =" + rec.getMilliSeconds());
        TestUtil.logMsg("Seqence no  =" + rec.getSequenceNumber());
        TestUtil.logMsg("Message     =" + rec.getMessage());
        if (rec.getClassName() != null) {
            TestUtil.logMsg("Class name  =" + rec.getClassName());
        }
        if (rec.getMethodName() != null) {
            TestUtil.logMsg("Method name =" + rec.getMethodName());
        }
        if (rec.getLevel() != null) {
            TestUtil.logMsg("Level        =" + rec.getLevel());
        }
        if (rec.getThrown() != null) {
            TestUtil.logMsg("Thrown       =" + rec.getThrown());
        }
        TestUtil.logMsg("");
    }

    public String extractQueryToken(String str, String ContextId) {
        StringTokenizer strtok;
        String DELIMETER = "|";
        String qstring = null;
        String qparams = null;

        strtok = new StringTokenizer(ContextId, DELIMETER);
        if (ContextId.indexOf(DELIMETER) > 0) {
            qstring = strtok.nextToken();
            if (strtok.hasMoreTokens()) {
                qparams = strtok.nextToken();
            }
        }

        // return query string or query params based on the content
        // of the string str
        if (str.equals("LogQueryString")) {
            return qstring;
        } else {
            return qparams;
        }
    }

    // This method tokenize the given string and
    // return first token and the remaining
    // string a string array based on the given delimeter
    public static String[] getTokens(String str, String delimeter) {
        String[] array = new String[2];
        StringTokenizer strtoken;

        // Get first token and the remaining string
        strtoken = new StringTokenizer(str, delimeter);
        if (str.indexOf(delimeter) > 0) {
            array[0] = strtoken.nextToken();
            array[1] = str.substring(array[0].length() + 3, str.length());
        }

        // TestUtil.logMsg("Input String ="+str);
        // TestUtil.logMsg("array[0] ="+array[0]);
        // TestUtil.logMsg("array[1] ="+array[1]);
        return array;
    }

    //
    // Locates the logs based on the given prefix string
    //
    // For example to locate all commit records i.e records such as
    //
    // commit :: MyApp1058312446320 , recordTimeStamp=1058312446598
    //
    // Use the following method to pull all the commit records
    //
    // fingLogsByPrefix("commit", nodes);
    public Collection findLogsByPrefix(String queryParams, NodeList nodes) throws Exception {
        Collection recordCollection = new Vector();
        String nodeName;
        String nodeValue;
        Node childNode;
        Node recordNode;
        NodeList recordNodeChildren;

        for (int i = 0; i < nodes.getLength(); i++) {
            // Take the first record
            recordNode = nodes.item(i);

            // get all the child nodes for the first record
            recordNodeChildren = recordNode.getChildNodes();

            for (int j = 0; j < recordNodeChildren.getLength(); j++) {
                childNode = recordNodeChildren.item(j);
                nodeName = childNode.getNodeName();
                if (nodeName.equals("message")) {
                    nodeValue = getText(childNode);
                    if (nodeValue.startsWith(queryParams)) {
                        // create a new record entry and
                        // add it to the collection
                        LogRecordEntry recordEntry = new LogRecordEntry(recordNode);

                        recordCollection.add(recordEntry);
                    }
                }
            }
        }
        return recordCollection;
    }

    public String getText(Node textNode) {
        String result = "";
        NodeList nodes = textNode.getChildNodes();

        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);

            if (node.getNodeType() == Node.TEXT_NODE) {
                result = node.getNodeValue();
                break;
            }
        }
        return result;
    }

}
