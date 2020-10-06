// Based on HostInfoReporter.java (from Stuart Clayman)

package mon.lattice.appl.reporters;

import mon.lattice.core.Measurement;
import mon.lattice.core.Timestamp;
import mon.lattice.core.ProbeValue;
import mon.lattice.core.ProbeValueWithName;
import mon.lattice.distribution.WithNames;

import cc.clayman.logging.BitMask;
import cc.clayman.logging.Logger;
import cc.clayman.logging.MASK;

import java.util.HashMap;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import mon.lattice.core.AbstractControllableReporter;
import mon.lattice.core.ReporterMeasurementType;
import org.slf4j.LoggerFactory;


/**
 *
 * An implementation of a Reporter that logs ProcessInfo data to a file.
 * 
 * This reports UDPSocketInfo measurements generated by the probe
 * mon.lattice.appl.probes.host.linux.UDPSocketInfo
 */
public class UDPSocketInfoReporter extends AbstractControllableReporter implements ReporterMeasurementType {
    String filename;

    // Stream for output
    FileOutputStream outputStream = null;

    HashMap<String, ProbeValue> values = null;


    // An elapsed time
    long elapsed = 0;

    /**
     * Constructor with reporter name and log file name
     */
    public UDPSocketInfoReporter(String reporterName, String filename) {
        super(reporterName);
        this.filename = filename;
    }    
    
    
    
    /**
     * Constructor with filename of log file
     */
    public UDPSocketInfoReporter(String filename) {
        super("UDPSocketInfoReporter");
        this.filename = filename;
    }
           
    /**
     * Report the Measurement
     */
    @Override
    public void report(Measurement m) {
        measurementToMap(m);

        Timestamp timestamp = m.getTimestamp();

        // host
        ProbeValue hostname = getAttribute("hostName");
        ProbeValue socketAddress = getAttribute("local_address");
        ProbeValue localPort = getAttribute("local_port");
        
        ProbeValue txQueue = getAttribute("tx_queue");
        ProbeValue rxQueue = getAttribute("rx_queue");
        ProbeValue drops = getAttribute("drops");
        
        String netLine = timestamp.value() + " " + Timestamp.elapsed(elapsed) + " N " + 
                         hostname.getValue() + " " +
                         socketAddress.getValue() + " " +
                         localPort.getValue() + " " + 
                         txQueue.getValue()  + " " + 
                         rxQueue.getValue()  + " " +
                         drops.getValue();
        
        Logger.getLogger("UDPSocketInfoReporter").logln(MASK.APP, netLine);

        // now add on the measurement delta
        elapsed += m.getDeltaTime().value();
    }

    
    /**
     * Init the Reporter.  Opens the log file.
     */
    @Override
    public void init() throws Exception {
        // allocate a new logger
        Logger logger = Logger.getLogger("UDPSocketInfoReporter");

        // add some extra output channels, using mask bit 6
        try {
            outputStream = new FileOutputStream(filename);
            logger.addOutput(new PrintWriter(outputStream), new BitMask(MASK.APP));
        } catch (Exception e) {
            LoggerFactory.getLogger(UDPSocketInfoReporter.class).error(e.getMessage());
        }

    }

    /**
     * Cleanup the Reporter. Closes the log file.
     */
    @Override
    public void cleanup() throws Exception {
        outputStream.close();
    }

    /**
     * convert the Measurement ProbeValues to a Map
     */
    protected void measurementToMap(Measurement m) {
        if (m instanceof WithNames) {
            values = new HashMap<String, ProbeValue>();
            for (ProbeValue pv : m.getValues()) {
                values.put(((ProbeValueWithName)pv).getName(), pv);
            }
        } else {
            LoggerFactory.getLogger(UDPSocketInfoReporter.class).error("UDPSocketInfo works with Measurements that are WithNames");
        }
    }
    
    /**
     * Get an attribute by name
     */
    protected ProbeValue getAttribute(String name) {
        if (values != null) {
            return values.get(name);
        } else {
            return null;
        }
    }

    @Override
    public List<String> getMeasurementTypes() {
        List<String> list = new ArrayList<>();
        list.add("UDPSocketInfo");
        return list;
    }

    
    
}
