package com.redhat.salud;

import jakarta.enterprise.context.ApplicationScoped;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.http.ProtocolException;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Bundle.BundleType;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@ApplicationScoped
public class CamelRoutes extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        from("file:{{input}}").routeId("csv-fhir-server")
                .onException(ProtocolException.class)
                    .handled(true)
                    .log(LoggingLevel.ERROR, "Error connecting to FHIR server with URL:{{serverUrl}}, please check the application.properties file ${exception.message}")
                .end()
                .log("Converting ${file:name}")
                .unmarshal().csv()
                .process(exchange -> {
                    //List<Patient> bundle = new ArrayList<>();
                    Bundle bundlePatient = new Bundle();
                    bundlePatient.setId("bundle-simplificado-001");
                    bundlePatient.setIdentifier(new Identifier().setValue("001"));
                    bundlePatient.setType(BundleType.TRANSACTION);
                    @SuppressWarnings("unchecked")
                    List<List<String>> patients = (List<List<String>>) exchange.getIn().getBody();
                    for (List<String> patient: patients) {
                        Patient fhirPatient = new Patient();
                        // DNI
                        fhirPatient.setId(patient.get(0));
                        HumanName humanName = new HumanName();
                        // Given name
                        humanName.addGiven(patient.get(1));
                        // Family name
                        humanName.setFamily(patient.get(2));
                        fhirPatient.setName(new ArrayList<>(Arrays.asList(humanName)));
                        Date patientBirthDateFormat = new SimpleDateFormat("yyyy-MM-dd").parse(patient.get(3));
                        fhirPatient.setBirthDate(patientBirthDateFormat);
                        //bundle.add(fhirPatient);
                        bundlePatient.addEntry().setResource(fhirPatient).getRequest().setMethod(Bundle.HTTPVerb.POST);                        
                    }
                    exchange.getIn().setBody(bundlePatient);
                })
                // create Patient in our FHIR server
                .to("fhir://transaction/withBundle?inBody=bundle&serverUrl={{serverUrl}}&fhirVersion={{fhirVersion}}&validationMode=NEVER")
                // log the outcome
                .log("Patients created successfully: ${body}");

    }

}