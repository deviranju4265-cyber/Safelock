const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const STUDENT_UID = "student_alice";
const OTHER_UID = "other_bob";
const STUDENT_ID = "SL-A7K92";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      host: emulatorHost,
      port: emulatorPort,
      rules: fs.readFileSync("firestore.rules", "utf8"),
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated read of student profile fails", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("students").doc(STUDENT_ID).get());
});

test("Student can create their own profile", async () => {
  const studentDb = testEnv.authenticatedContext(STUDENT_UID).firestore();
  const now = new Date();
  await assertSucceeds(
    studentDb.collection("students").doc(STUDENT_ID).set({
      studentId: STUDENT_ID,
      userId: STUDENT_UID,
      studentName: "Alice Smith",
      deviceModel: "Pixel 8",
      pairingStatus: "unpaired",
      parentUids: [],
      activeScheduleId: null,
      isLockedManually: false,
      emergencyUnlockUntil: null,
      createdAt: now,
      updatedAt: now,
    })
  );
});

test("Cross-user cannot overwrite student profile", async () => {
  const studentDb = testEnv.authenticatedContext(STUDENT_UID).firestore();
  const otherDb = testEnv.authenticatedContext(OTHER_UID).firestore();
  const now = new Date();

  await assertSucceeds(
    studentDb.collection("students").doc(STUDENT_ID).set({
      studentId: STUDENT_ID,
      userId: STUDENT_UID,
      studentName: "Alice Smith",
      pairingStatus: "unpaired",
      createdAt: now,
      updatedAt: now,
    })
  );

  await assertFails(
    otherDb.collection("students").doc(STUDENT_ID).update({
      studentName: "Hacked by Bob",
      updatedAt: new Date(),
    })
  );
});

test("Student can submit permission request", async () => {
  const studentDb = testEnv.authenticatedContext(STUDENT_UID).firestore();
  const now = new Date();
  await assertSucceeds(
    studentDb.collection("student_requests").doc("req_123").set({
      requestId: "req_123",
      studentId: STUDENT_ID,
      userId: STUDENT_UID,
      type: "emergency_unlock",
      details: "Need 15 minutes to call tutor",
      status: "pending",
      parentDecisionNote: null,
      createdAt: now,
      updatedAt: now,
    })
  );
});
