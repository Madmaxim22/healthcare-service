package ru.netology.patient.service.medical;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import ru.netology.patient.entity.BloodPressure;
import ru.netology.patient.entity.HealthInfo;
import ru.netology.patient.entity.PatientInfo;
import ru.netology.patient.repository.PatientInfoFileRepository;
import ru.netology.patient.service.alert.SendAlertService;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
class MedicalServiceImplTest {

    @Mock
    PatientInfoFileRepository patientInfoFileRepository;
    SendAlertService sendAlertService;
    MockitoSession mockitoSession;
    @Captor
    ArgumentCaptor<String> requestCaptor;

    @BeforeMethod
    public void beforeMethod() {
        mockitoSession = Mockito.mockitoSession()
                        .initMocks(this)
                        .startMocking();
    }

    @AfterMethod
    public void afterMethod() {
        mockitoSession.finishMocking();
    }

    private static Stream<Arguments> newPatient() {
        return Stream.of(
                Arguments.of("Warning, patient with id: 7509d79b-0c39-4d31-9d97-79d9f58522e2, need help",
                        new PatientInfo("7509d79b-0c39-4d31-9d97-79d9f58522e2","Иван", "Петров", LocalDate.of(1980, 11, 26),
                                new HealthInfo(new BigDecimal("36.65"), new BloodPressure(120, 80)))),
                Arguments.of("Warning, patient with id: e854a5d2-dd73-421d-bf37-183c7e2eec66, need help",
                        new PatientInfo("e854a5d2-dd73-421d-bf37-183c7e2eec66","Семен", "Михайлов", LocalDate.of(1982, 1, 16),
                                new HealthInfo(new BigDecimal("36.6"), new BloodPressure(125, 78))))
        );
    }

    @ParameterizedTest
    @MethodSource("newPatient")
    void checkBloodPressureSendTest(String expected, PatientInfo patient) {
        patientInfoFileRepository = Mockito.mock();
        Mockito.when(patientInfoFileRepository.getById(any()))
                .thenReturn(patient);

        sendAlertService = Mockito.mock();

        MedicalService medicalService = new MedicalServiceImpl(patientInfoFileRepository, sendAlertService);
        medicalService.checkBloodPressure(patient.getId(), new BloodPressure(60, 120));

        requestCaptor = ArgumentCaptor.forClass(String.class);
        verify(sendAlertService).send(requestCaptor.capture());

        verify(sendAlertService, times(1)).send(any());
        assertThat(requestCaptor.getValue(), equalTo(expected));
    }

    @Test
    void normalBloodPressureTest() {
        patientInfoFileRepository = Mockito.mock();
        Mockito.when(patientInfoFileRepository.getById(any()))
                .thenReturn(new PatientInfo("7509d79b-0c39-4d31-9d97-79d9f58522e2","Иван", "Петров", LocalDate.of(1980, 11, 26),
                        new HealthInfo(new BigDecimal("36.65"), new BloodPressure(120, 80))));

        sendAlertService = Mockito.mock();

        MedicalService medicalService = new MedicalServiceImpl(patientInfoFileRepository, sendAlertService);
        medicalService.checkBloodPressure("7509d79b-0c39-4d31-9d97-79d9f58522e2", new BloodPressure(120, 80));

        verify(sendAlertService, never()).send(any());
    }

    @ParameterizedTest
    @MethodSource("newPatient")
     void checkTemperatureTest(String expected, PatientInfo patient) {
        patientInfoFileRepository = Mockito.mock();
        Mockito.when(patientInfoFileRepository.getById(any()))
                .thenReturn(patient);

        sendAlertService = Mockito.mock();

        MedicalService medicalService = new MedicalServiceImpl(patientInfoFileRepository, sendAlertService);

        String consoleOutput = null;
        PrintStream originalOut = System.out;
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream(100);
            PrintStream capture = new PrintStream(outputStream);
            System.setOut(capture);
            medicalService.checkTemperature(patient.getId(), new BigDecimal("30.9"));
            capture.flush();
            consoleOutput = outputStream.toString();
            System.setOut(originalOut);
        } catch (Exception e) {
            e.printStackTrace();
        }
        requestCaptor = ArgumentCaptor.forClass(String.class);
        verify(sendAlertService).send(requestCaptor.capture());

        assertThat(consoleOutput, equalTo(expected));
        assertThat(requestCaptor.getValue(), equalTo(expected));
    }

    @Test
    void normalTemperatureTest() {
        patientInfoFileRepository = Mockito.mock();
        Mockito.when(patientInfoFileRepository.getById(any()))
                .thenReturn(new PatientInfo("7509d79b-0c39-4d31-9d97-79d9f58522e2","Иван", "Петров", LocalDate.of(1980, 11, 26),
                        new HealthInfo(new BigDecimal("36.65"), new BloodPressure(120, 80))));

        sendAlertService = Mockito.mock();

        MedicalService medicalService = new MedicalServiceImpl(patientInfoFileRepository, sendAlertService);
        medicalService.checkTemperature("7509d79b-0c39-4d31-9d97-79d9f58522e2", new BigDecimal("36.65"));

        verify(sendAlertService, never()).send(any());
    }
}