const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
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

test("Unauthenticated user cannot read or write brews", async () => {
  const unauthedDb = testEnv.unauthenticatedContext().firestore();
  const brewRef = unauthedDb.doc(`users/${ALICE_UID}/brews/brew1`);
  await assertFails(brewRef.get());
  await assertFails(
    brewRef.set({
      userId: ALICE_UID,
      beanName: "Ethiopia Yirgacheffe",
      recipeName: "4:6 Kasuya",
      doseGrams: 15,
      ratio: 15,
      totalWaterGrams: 225,
      durationSeconds: 210,
      createdAt: new Date(),
    })
  );
});

test("Alice can create and read her own brew record", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const brewRef = aliceDb.doc(`users/${ALICE_UID}/brews/brew1`);

  await assertSucceeds(
    brewRef.set({
      userId: ALICE_UID,
      beanName: "Gayo Natural",
      recipeName: "James Hoffmann V60",
      doseGrams: 15,
      ratio: 16,
      totalWaterGrams: 240,
      durationSeconds: 195,
      rating: 5,
      tastingNotes: "Jasmine, bergamot, peach sweetness",
      createdAt: new Date(),
    })
  );

  await assertSucceeds(brewRef.get());
});

test("Bob cannot read or write Alice's brew records", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await aliceDb.doc(`users/${ALICE_UID}/brews/brew1`).set({
    userId: ALICE_UID,
    beanName: "Gayo Natural",
    recipeName: "James Hoffmann V60",
    doseGrams: 15,
    ratio: 16,
    totalWaterGrams: 240,
    durationSeconds: 195,
    createdAt: new Date(),
  });

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(bobDb.doc(`users/${ALICE_UID}/brews/brew1`).get());
  await assertFails(
    bobDb.doc(`users/${ALICE_UID}/brews/brew2`).set({
      userId: ALICE_UID,
      beanName: "Malicious Brew",
      recipeName: "Attack",
      doseGrams: 15,
      ratio: 15,
      totalWaterGrams: 225,
      durationSeconds: 180,
      createdAt: new Date(),
    })
  );
});

test("Alice cannot write invalid payload (e.g. dose out of range)", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const brewRef = aliceDb.doc(`users/${ALICE_UID}/brews/invalid_brew`);

  await assertFails(
    brewRef.set({
      userId: ALICE_UID,
      beanName: "Test",
      recipeName: "Test",
      doseGrams: 9999, // exceeds max 200
      ratio: 15,
      totalWaterGrams: 225,
      durationSeconds: 180,
      createdAt: new Date(),
    })
  );
});

test("Alice can create and read her custom recipe", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const recipeRef = aliceDb.doc(`users/${ALICE_UID}/custom_recipes/rec1`);

  await assertSucceeds(
    recipeRef.set({
      userId: ALICE_UID,
      name: "Seduh Favorit Pagi",
      methodType: "KASUYA_4_6",
      doseGrams: 16,
      ratio: 15,
      createdAt: new Date(),
    })
  );

  await assertSucceeds(recipeRef.get());

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(bobDb.doc(`users/${ALICE_UID}/custom_recipes/rec1`).get());
});
