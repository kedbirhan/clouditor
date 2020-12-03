package io.clouditor.rest;

import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.clouditor.Engine;
import io.clouditor.assurance.*;
import io.clouditor.discovery.DiscoveryService;
import java.util.*;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Assertions;
import org.mockito.Mockito;

public class CertificationResourceTest extends JerseyTest {
  // @Mock
  private static final Engine engine = new Engine();

  private static final Logger LOGGER = LogManager.getLogger();
  private static final String ASSET_TYPE = "fake";
  // @InjectMocks
  CertificationResource certificationResource;
  // @Mock
  CertificationResource certificationServiceMock = Mockito.mock(CertificationResource.class);
  private String token;

  // @Mock Engine engine;

  @AfterAll
  static void cleanUpOnce() {
    engine.shutdown();
  }

  @BeforeAll
  static void startUpOnce() {
    engine.setDbInMemory(true);

    engine.setDBName("EngineAPIResorcesTestDB");

    // init db
    engine.initDB();

    // initialize every else
    engine.init();

    // start the DiscoveryService
    engine.getService(DiscoveryService.class).start();
  }

  /*
  ToDo: Check if private fields Engine engine and CertificationService service of
  CertificationResource are not null
  Is it possible to mock it?
   */
  @Test
  public void testCertificationResource_constructor() {
    target("certification")
        .request()
        .header(
            AuthenticationFilter.HEADER_AUTHORIZATION,
            AuthenticationFilter.createAuthorization(token))
        .get();
  }

  /*
   * ToDo: The first if condition cannot be evaluated to true since before getCertification(cert) would do it  but...
   * ToDo: Is it even possible to catch the exception if it takes place in getCertification(cert)?
   */
  @Test
  public void givenModifyControlStatus_whenCertificationIsNullAndControlIsNull_thenThrowError() {

    assertThrows(
        NotFoundException.class,
        () -> {
          target("certification/1/1/status")
              .request()
              .header(
                  AuthenticationFilter.HEADER_AUTHORIZATION,
                  AuthenticationFilter.createAuthorization(token))
              .post(Entity.json("{}"));
        });
  }

  /*
   *  Todo: Doesn't throw NotFoundException even though code coverage stops there
   * */
  // @Test
  public void
      givenModifyControlStatus_whenCertificationNotNullAndControlIsNull_thenThrowException() {
    assertThrows(
        NotFoundException.class,
        () -> {
          CertificationService certService = engine.getService(CertificationService.class);
          Certification mockCertification = new Certification();
          mockCertification.setId("1");
          certService.modifyCertification(mockCertification);

          Response response =
              target("certification/1/1/status")
                  .request()
                  .header(
                      AuthenticationFilter.HEADER_AUTHORIZATION,
                      AuthenticationFilter.createAuthorization(token))
                  .post(Entity.json("{}"));
          System.out.println("StringTest: " + response);
        });
  }

  /*
  Hibernate Error: Domain wasn't set
  But why hibernate doesn't have a problem with rules or evaluations ? They are manyToMany?
   */
  @Test
  public void givenModifyControlStatus_whenCertificationNotNullAndControlNotNull_thenStatus204() {
    // Create mock of certification  with id=1 (the one that will be searched for in
    // testModifyControlStatus())
    CertificationService certService = engine.getService(CertificationService.class);
    Certification mockCertification = new Certification();
    mockCertification.setId("1");
    // Print for debugging purposes
    System.out.println(mockCertification);

    // Create mock of control with id=2 (will be attached to the mocked certification)
    Control mockControl = new Control();
    // mockControl.setAutomated(true);
    mockControl.setControlId("2");
    mockControl.setDomain(new Domain("TestDomain"));

    // build a simple rule
    //    var rule = new Rule();
    //    rule.addCondition(new CCLDeserializer().parse("MockAsset has property == true"));
    //    var ruleService = this.engine.getService(RuleService.class);
    //    ruleService.getRules().put("MockAsset", Set.of(rule));
    //    mockControl.setRules(List.of(rule));
    //    certService.startMonitoring(mockControl);
    //    mockControl.evaluate(this.engine.getServiceLocator());
    //    certService.modifyCertification(mockCertification);

    // Add mocked control (as list of one control) to mocked certification
    List<Control> oneControlList = new ArrayList<>();
    oneControlList.add(mockControl);
    mockCertification.setControls(oneControlList);
    // Check if control is inside
    System.out.println(mockCertification);

    // Update the certificate in the certification service
    certService.modifyCertification(mockCertification);

    Response response =
        target("certification/1/2/status")
            .request()
            .header(
                AuthenticationFilter.HEADER_AUTHORIZATION,
                AuthenticationFilter.createAuthorization(this.token))
            .post(Entity.entity("{}", MediaType.APPLICATION_JSON_TYPE));
    System.out.println("StringTest: " + response);
    assertEquals(
        "HTTP Response should be 204, no content: ",
        Response.Status.NO_CONTENT.getStatusCode(),
        response.getStatus());
  }

  /*
  2
   */
  @Test
  public void givenModifyControlStatus_whenStatusFalseAndControlActiveTrue_thenStopMonitoring() {
    // Create mock of certification  with id=1 (the one that will be searched for in
    // testModifyControlStatus())
    CertificationService certService = engine.getService(CertificationService.class);
    Certification mockCertification = new Certification();
    mockCertification.setId("1");
    // Print for debugging purposes
    System.out.println(mockCertification);

    // Create mock of control with id=2 (will be attached to the mocked certification)
    Control mockControl = new Control();
    // mockControl.setAutomated(true);
    mockControl.setControlId("2");
    mockControl.setDomain(new Domain("TestDomain"));
    mockControl.setActive(true);

    // build a simple rule
    //    var rule = new Rule();
    //    rule.addCondition(new CCLDeserializer().parse("MockAsset has property == true"));
    //    var ruleService = this.engine.getService(RuleService.class);
    //    ruleService.getRules().put("MockAsset", Set.of(rule));
    //    mockControl.setRules(List.of(rule));
    //    certService.startMonitoring(mockControl);
    //    mockControl.evaluate(this.engine.getServiceLocator());
    //    certService.modifyCertification(mockCertification);

    // Add mocked control (as list of one control) to mocked certification
    List<Control> oneControlList = new ArrayList<>();
    oneControlList.add(mockControl);
    mockCertification.setControls(oneControlList);
    // Check if control is inside
    System.out.println(mockCertification);

    // Update the certificate in the certification service
    certService.modifyCertification(mockCertification);

    // Create ControlStatusRequest
    // ToDo: Could you mock CertificationResource.ControlStatusRequest ? (Instead of creating own
    // test class here)
    CertificationResourceTest.ControlStatusRequest controlStatusRequest =
        new CertificationResourceTest.ControlStatusRequest(false);

    Response response =
        target("certification/1/2/status")
            .request()
            .header(
                AuthenticationFilter.HEADER_AUTHORIZATION,
                AuthenticationFilter.createAuthorization(this.token))
            .post(Entity.entity(controlStatusRequest, MediaType.APPLICATION_JSON_TYPE));
    System.out.println("StringTest: " + response);
    assertEquals(
        "HTTP Response should be 204: ",
        Response.Status.NO_CONTENT.getStatusCode(),
        response.getStatus());
    boolean mockControlActive = mockControl.isActive();
    Assertions.assertFalse(mockControlActive);
  }

  /*
  2
   */
  @Test
  public void givenModifyControlStatus_whenStatusTrueAndControlActiveFalse_thenStartMonitoring() {
    // Create mock of certification  with id=1 (the one that will be searched for in
    // testModifyControlStatus())
    CertificationService certService = engine.getService(CertificationService.class);
    Certification mockCertification = new Certification();
    mockCertification.setId("1");
    // Print for debugging purposes
    System.out.println(mockCertification);

    // Create mock of control with id=2 (will be attached to the mocked certification)
    Control mockControl = new Control();
    // mockControl.setAutomated(true);
    mockControl.setControlId("2");
    mockControl.setDomain(new Domain("TestDomain"));
    mockControl.setActive(false);

    // build a simple rule
    //    var rule = new Rule();
    //    rule.addCondition(new CCLDeserializer().parse("MockAsset has property == true"));
    //    var ruleService = this.engine.getService(RuleService.class);
    //    ruleService.getRules().put("MockAsset", Set.of(rule));
    //    mockControl.setRules(List.of(rule));
    //    certService.startMonitoring(mockControl);
    //    mockControl.evaluate(this.engine.getServiceLocator());
    //    certService.modifyCertification(mockCertification);

    // Add mocked control (as list of one control) to mocked certification
    List<Control> oneControlList = new ArrayList<>();
    oneControlList.add(mockControl);
    mockCertification.setControls(oneControlList);
    // Check if control is inside
    System.out.println(mockCertification);

    // Update the certificate in the certification service
    certService.modifyCertification(mockCertification);

    // Create ControlStatusRequest
    // ToDo: Could you mock CertificationResource.ControlStatusRequest ? (Instead of creating own
    // test class here)
    CertificationResourceTest.ControlStatusRequest controlStatusRequest =
        new CertificationResourceTest.ControlStatusRequest(true);

    Assertions.assertFalse(mockControl.isActive());
    // ToDo: Test this better (with response maybe?)
    Assertions.assertTrue(controlStatusRequest.status);
    Response response =
        target("certification/1/2/status")
            .request()
            .header(
                AuthenticationFilter.HEADER_AUTHORIZATION,
                AuthenticationFilter.createAuthorization(this.token))
            .post(Entity.entity(controlStatusRequest, MediaType.APPLICATION_JSON_TYPE));
    System.out.println("StringTest: " + response);
    assertEquals(
        "HTTP Response should be 204: ",
        Response.Status.NO_CONTENT.getStatusCode(),
        response.getStatus());
    boolean mockControlActive = mockControl.isActive();
    Assertions.assertFalse(mockControlActive);
  }

  @Test
  public void givenGetCertifications_whenCorrectRequest_thenResponseIsOkAndContainsCertificate() {
    CertificationService certService = engine.getService(CertificationService.class);
    Certification mockCertification = new Certification();
    mockCertification.setId("Mock1");

    certService.modifyCertification(mockCertification);

    Response response =
        target("/certification/Mock1")
            .request()
            .header(
                AuthenticationFilter.HEADER_AUTHORIZATION,
                AuthenticationFilter.createAuthorization(token))
            .get();
    // response.readEntity()
    // assertEquals("Mock1", response.get);
    // assertEquals("HTTP Response should be 200: ", Status.OK.getStatusCode(),
    // response.getStatus());
    assertEquals(
        "Http Content-Type should be: ",
        MediaType.APPLICATION_JSON,
        response.getHeaderString(HttpHeaders.CONTENT_TYPE));
  }

  @Test
  public void givenGetCertifications_whenTwoCertificationsAvailable_thenStatusOkAndReturnBoth() {
    // Create two mocks of certification  with id=1 and id=2, respectively
    CertificationService certService = engine.getService(CertificationService.class);
    Certification mockCertification1 = new Certification();
    mockCertification1.setId("1");
    certService.modifyCertification(mockCertification1);
    Certification mockCertification2 = new Certification();
    mockCertification2.setId("4");
    certService.modifyCertification(mockCertification2);

    // ToDo: Combine both get requests in one
    // Execute first get request
    Response response1 =
        target("/certification")
            .request()
            .header(
                AuthenticationFilter.HEADER_AUTHORIZATION,
                AuthenticationFilter.createAuthorization(token))
            .get();
    assertEquals(Response.Status.OK.getStatusCode(), response1.getStatus());

    // Execute second get request
    Map certifications =
        target("/certification")
            .request()
            .header(
                AuthenticationFilter.HEADER_AUTHORIZATION,
                AuthenticationFilter.createAuthorization(token))
            .get(HashMap.class);
    assertFalse(certifications.isEmpty());
    assertTrue(certifications.containsKey(mockCertification1.getId()));
    assertTrue(certifications.containsKey(mockCertification2.getId()));
  }

  @Test
  public void
      givenGetCertifications_whenNoCertificationsAvailable_thenStatusOKButEmptyResponseContent() {

    // ToDo: Combine both get requests in one
    // Execute first get request
    Response response =
        target("/certification")
            .request()
            .header(
                AuthenticationFilter.HEADER_AUTHORIZATION,
                AuthenticationFilter.createAuthorization(token))
            .get();
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    System.out.println(response);

    // Execute second get request
    Map certifications =
        target("/certification")
            .request()
            .header(
                AuthenticationFilter.HEADER_AUTHORIZATION,
                AuthenticationFilter.createAuthorization(token))
            .get(HashMap.class);
    System.out.println(certifications);
    assertTrue(certifications.isEmpty());
  }

  @Test
  public void givenGetCertification_whenOneCertificationAvailable_thenStatusOkAndReturnIt() {
    // Create one mock of certification  with id=1
    CertificationService certService = engine.getService(CertificationService.class);
    Certification mockCertification1 = new Certification();
    mockCertification1.setId("1");
    mockCertification1.setDescription("Test Description");
    certService.modifyCertification(mockCertification1);

    // ToDo: Combine both get requests in one
    // Execute first get request
    Response response1 =
        target("/certification/1")
            .request()
            .header(
                AuthenticationFilter.HEADER_AUTHORIZATION,
                AuthenticationFilter.createAuthorization(token))
            .get();
    assertEquals(Response.Status.OK.getStatusCode(), response1.getStatus());

    // Execute second get request
    Certification certification =
        target("/certification/1")
            .request()
            .header(
                AuthenticationFilter.HEADER_AUTHORIZATION,
                AuthenticationFilter.createAuthorization(token))
            .get(Certification.class);
    assertEquals(mockCertification1.getId(), certification.getId());
    assertEquals(mockCertification1.getDescription(), certification.getDescription());
  }

  @Test
  public void
      givenGetCertification_whenNoCertificationAvailableWithGivenId_thenStatus404AndThrowException() {

    // ToDo: Combine both get requests in one
    // Execute first get request
    Response response =
        target("/certification/66")
            .request()
            .header(
                AuthenticationFilter.HEADER_AUTHORIZATION,
                AuthenticationFilter.createAuthorization(token))
            .get();
    assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
    System.out.println(response);

    assertThrows(
        NotFoundException.class,
        () -> {
          // Execute second get request
          Certification certification =
              target("/certification/1")
                  .request()
                  .header(
                      AuthenticationFilter.HEADER_AUTHORIZATION,
                      AuthenticationFilter.createAuthorization(token))
                  .get(Certification.class);
        });
  }

  /*
   * ToDo: Exception is caught but not marked as covered*/
  @Test
  public void givenGetControl_whenNoControlAvailableWithGivenId_thenStatus404AndThrowException() {

    // ToDo: Combine both get requests in one
    // Execute first get request (get status code)
    Response response1 =
        // Todo: use String format for replacing 1 and 2 (#magicNumbers)
        target("/certification/1/2")
            .request()
            .header(
                AuthenticationFilter.HEADER_AUTHORIZATION,
                AuthenticationFilter.createAuthorization(token))
            .get();
    assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response1.getStatus());

    // Execute second get request (get content)
    assertThrows(
        NotFoundException.class,
        () -> {
          // Execute second get request
          Certification certification =
              target("/certification/1/2")
                  .request()
                  .header(
                      AuthenticationFilter.HEADER_AUTHORIZATION,
                      AuthenticationFilter.createAuthorization(token))
                  .get(Certification.class);
        });
  }
  /*
   * ToDo: Method is covered but '500 internal server error' occurs. Response type?
   *  */
  @Test
  public void givenGetControl_whenControlAvailableWithGivenId_thenStatusOkAndReturnIt() {
    // Create mock of certification  with id=1 (the one that will be searched for in
    // testModifyControlStatus())
    CertificationService certService = engine.getService(CertificationService.class);
    Certification mockCertification = new Certification();
    mockCertification.setId("1");
    // Print for debugging purposes
    System.out.println(mockCertification);

    // Create mock of control with id=2 (will be attached to the mocked certification)
    Control mockControl = new Control();
    // mockControl.setAutomated(true);
    mockControl.setControlId("2");
    mockControl.setDomain(new Domain("TestDomain"));

    // build a simple rule
    //    var rule = new Rule();
    //    rule.addCondition(new CCLDeserializer().parse("MockAsset has property == true"));
    //    var ruleService = this.engine.getService(RuleService.class);
    //    ruleService.getRules().put("MockAsset", Set.of(rule));
    //    mockControl.setRules(List.of(rule));
    //    certService.startMonitoring(mockControl);
    //    mockControl.evaluate(this.engine.getServiceLocator());
    //    certService.modifyCertification(mockCertification);

    // Add mocked control (as list of one control) to mocked certification
    List<Control> oneControlList = new ArrayList<>();
    oneControlList.add(mockControl);
    mockCertification.setControls(oneControlList);
    // Check if control is inside
    System.out.println(mockCertification);

    // Update the certificate in the certification service
    certService.modifyCertification(mockCertification);

    Response response =
        target("certification/1/2")
            .request()
            .header(
                AuthenticationFilter.HEADER_AUTHORIZATION,
                AuthenticationFilter.createAuthorization(this.token))
            .get();
    System.out.println(response);
    Assertions.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    // ToDo: Check the returned object
  }

  /*
   * ToDo: Catch Exception
   *  */
  @Test
  public void
      givenImportCertification_whenNoCertificationAvailable_thenStatus404AndThrowException() {
    //    CertificationService certService = engine.getService(CertificationService.class);

    //    assertTrue(certService.getCertifications().isEmpty());
    // Execute first Post Request (for status)
    Response response1 =
        target("certification/import/1")
            .request()
            .header(
                AuthenticationFilter.HEADER_AUTHORIZATION,
                AuthenticationFilter.createAuthorization(this.token))
            .post(Entity.json("{}"));
    Assertions.assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response1.getStatus());
  }

  /*
   *
   *  */
  @Test
  public void
      givenImportCertification_whenCertificationAvailableWithGivenId_thenStatusOkAndCertificateThere() {
    // Get CertificationService
    CertificationService certService = engine.getService(CertificationService.class);
    // Get Importers and create iterator for receiving one importer
    Map importers = certService.getImporters();
    Iterator<Map.Entry<String, CertificationImporter>> iterator = importers.entrySet().iterator();
    Map.Entry<String, CertificationImporter> firstCertificationImporter = iterator.next();

    // Verify that there are no certifications currently available (by checking the hash map of
    // certifications)
    assertTrue(certService.getCertifications().isEmpty());
    Response response =
        target("certification/import/BSI C5")
            .request()
            .header(
                AuthenticationFilter.HEADER_AUTHORIZATION,
                AuthenticationFilter.createAuthorization(this.token))
            .post(Entity.json("{}"));
    // Check if status is 204, No Content
    assertEquals(Response.Status.NO_CONTENT.getStatusCode(), response.getStatus());
    // Check if certification hashMap is not empty any more
    assertFalse(certService.getCertifications().isEmpty());
    // Check if the certification from firstCertificationImporter was properly imported
    assertNotNull(certService.getCertifications().get(firstCertificationImporter.getKey()));
  }

  @Test
  public void givenGetImporters_when_then() {
    // Get CertificationService
    CertificationService certService = engine.getService(CertificationService.class);
    // Get Importers
    Map importers = certService.getImporters();
    System.out.println(importers);
    Response response =
        target("certification/importers")
            .request()
            .header(
                AuthenticationFilter.HEADER_AUTHORIZATION,
                AuthenticationFilter.createAuthorization(this.token))
            .get();
    System.out.println(response);
  }

  @BeforeEach
  public void setUp() throws Exception {
    super.setUp();

    client().register(ObjectMapperResolver.class);

    if (this.token == null) {
      this.token = engine.authenticateAPI(target(), "clouditor", "clouditor");
    }
  }

  @AfterEach
  public void reset() throws Exception {
    // ToDo: Reset storage (remove certifications, controls,...)
  }

  @Override
  protected Application configure() {
    // Find first available port.
    forceSet(TestProperties.CONTAINER_PORT, "0");
    return new EngineAPI(engine);
  }

  public static class ControlStatusRequest {

    @JsonProperty private final boolean status;

    public ControlStatusRequest(boolean status) {
      this.status = status;
    }
  }
}
