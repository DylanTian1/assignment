package com.redenergy.demo;

import com.redenergy.demo.domain.MeterRead;
import com.redenergy.demo.parser.SimpleNem12Parser;
import com.redenergy.demo.parser.SimpleNem12ParserImplementation;
import org.springframework.boot.Banner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;
import java.util.Collection;

import static java.lang.System.exit;

@SpringBootApplication
public class DemoApplication implements CommandLineRunner{

	public static void main(String[] args) {

		SpringApplication app = new SpringApplication(DemoApplication.class);
		app.setBannerMode(Banner.Mode.OFF);
		app.run();
	}

	@Override
	public void run(String... args) throws Exception {

		File simpleNem12File;
		if (args.length == 1) {
			simpleNem12File = new File(args[0]);
		} else {
			ClassLoader classLoader = getClass().getClassLoader();
			simpleNem12File = new File(classLoader.getResource("SimpleNem12.csv").getFile());
		}

        SimpleNem12Parser simpleNem12Parser = new SimpleNem12ParserImplementation();
		Collection<MeterRead> meterReads = simpleNem12Parser.parseSimpleNem12(simpleNem12File);

		MeterRead read6123456789 = meterReads.stream().filter(mr -> mr.getNmi().equals("6123456789")).findFirst().get();
		System.out.println(String.format("Total volume for NMI 6123456789 is %f", read6123456789.getTotalVolume()));  // Should be -36.84

		MeterRead read6987654321 = meterReads.stream().filter(mr -> mr.getNmi().equals("6987654321")).findFirst().get();
		System.out.println(String.format("Total volume for NMI 6987654321 is %f", read6987654321.getTotalVolume()));  // Should be 14.33

		exit(0);
	}
}
