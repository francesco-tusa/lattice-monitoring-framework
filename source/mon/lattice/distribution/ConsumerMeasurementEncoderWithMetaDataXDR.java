package mon.lattice.distribution;

import java.io.DataOutput;
import java.io.IOException;
import mon.lattice.core.Measurement;
import mon.lattice.core.ConsumerMeasurement;
import mon.lattice.core.ID;
import mon.lattice.core.TypeException;
import mon.lattice.core.plane.MessageType;

/**
 * Convert a ConsumerMeasurement that has some Message Meta Data (without names) to a XDR representation.
 * It utilises MeasurementEncoderXDR, but it also uses the Meta Data 
 * to add on the Data Source ID, the Data Source seqNo, and the Message type.
 *
 * This extra data is useful in some contexts.
 */
public class ConsumerMeasurementEncoderWithMetaDataXDR {
    
    // The Measurement
    ConsumerMeasurement measurement;
    
    public ConsumerMeasurementEncoderWithMetaDataXDR(ConsumerMeasurement m) {
        measurement = m;
    }

    public ConsumerMeasurementEncoderWithMetaDataXDR(Measurement m) {
        measurement = (ConsumerMeasurement)m;
    }
    
    
    public void encode(DataOutput out) throws IOException, TypeException {

        // encode the measurement, ready for transmission
        // try and output some data source info
        // this is kept in the MetaData
        ConsumerMeasurementWithMetaData cm = (ConsumerMeasurementWithMetaData)measurement;

        MessageMetaData metaData =  (MessageMetaData)cm.getMessageMetaData();

        // a bit of knowledge of exposed fields
        ID dataSourceID = metaData.dataSourceID;
        int dataSourceSeqNo = metaData.seqNo;
        MessageType type = metaData.type;

        // write the DataSource id
        out.writeLong(dataSourceID.getMostSignificantBits());
        out.writeLong(dataSourceID.getLeastSignificantBits());

        // write type
        out.writeInt(type.getValue());

        // write seqNo
        out.writeInt(dataSourceSeqNo);
        
        encodeMeasurement(cm, out);
    }
    
    
    protected void encodeMeasurement(ConsumerMeasurementWithMetaData cm, DataOutput out) throws IOException, TypeException {
        // encode the measurement as XDR, ready for transmission
        MeasurementEncoderXDR encoder = new MeasurementEncoderXDR(cm);

        // encode into an existing XDR
        encoder.encode(out);
    }

}
