package de.fraunhofer.igd.geo.vgv.flink;

import java.util.*;
import org.apache.flink.api.java.operators.IterativeDataSet;
import org.apache.flink.core.fs.FileSystem;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.utils.ParameterTool;
public class BorderGermany {

private static final int MAX_IT = 2;
//public static double d = 0.0;
    public static void main(String... args) throws Exception {
	
        // Checking input parameters
        final ParameterTool params = ParameterTool.fromArgs(args);
        String dataFile = ""; // Path to the file with all geo locations.
        if (params.has("data")) {
            dataFile = params.get("data");
        } else {
            throw new RuntimeException("No --data attribute passed. Set the location of the input data file.");
        }

        // set up execution environment
        ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();
	env.setParallelism(1);
	DataSet<String> lines = env.readTextFile(args[1]);
	
	DataSet<Position> positions = lines.map(new MapFunction<String,Position>() {
		@Override
                public Position map(String value) throws Exception {
			String[] split = value.split(" ");
			return new Position(Double.valueOf(split[1]),Double.valueOf(split[2])); 
		}
	});
	DataSet<String> pos_end = lines.reduce(new ReduceFunction<String>() {
		@Override
		public String reduce(String s1, String s2) throws Exception {
			String[] split1 = s1.split(" ");
			String[] split2 = s2.split(" ");
			Position p1 = new Position(Double.valueOf(split1[1]), Double.valueOf(split1[2]));
			Position p2 = new Position(Double.valueOf(split2[1]), Double.valueOf(split2[2]));
			double d = 1.0;
			try {
				d = Double.valueOf(split1[0]) + p2.distanceTo(p1);
			} catch (NumberFormatException e)
			{
				d = Integer.valueOf(split1[0]) + p2.distanceTo(p1);
			}
			split2[0] = new String("" + d );
			return new String(split2[0] + " " + split2[1] + " " + split2[2]);
		}
	});
	DataSet<Double> borderlength = pos_end.map(new MapFunction <String,Double>(){
		@Override
		public Double map(String dist_lng_lat) throws Exception {
			String[] split = dist_lng_lat.split(" ");
			return Double.valueOf(split[0]);
		}
	});
//	borderlength.print();
	borderlength.writeAsText("./uebung01-2-result.txt",FileSystem.WriteMode.OVERWRITE);
//	List<Position> poslist = pos2.collect();
	env.execute(); 
//	for(int i = 0; i < poslist.size() - 1; i++){
//		b += poslist.get(i).distanceTo(poslist.get(i+1));
//	}
	System.out.println(" Done");
    }

}
