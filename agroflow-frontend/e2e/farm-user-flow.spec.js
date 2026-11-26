import { expect, test } from "@playwright/test";

test("user can register, create a farm, view details, and update it", async ({ page }) => {
  const uniqueId = Date.now();
  const email = `farm-e2e-${uniqueId}@example.com`;
  const farmName = `E2E Farm ${uniqueId}`;
  const updatedFarmName = `${farmName} Updated`;
  const farms = [];
  const irrigationZones = [];

  await page.route("http://localhost:8080/**", async (route) => {
    const request = route.request();
    const url = new URL(request.url());
    const method = request.method();
    const path = url.pathname;

    if (path === "/auth/register" && method === "POST") {
      return route.fulfill({
        json: {
          token: `token-${uniqueId}`,
          name: "E2E Farmer",
          email,
          role: "USER",
          message: "Account was successfully created.",
        },
      });
    }

    if (path === "/farms" && method === "GET") {
      return route.fulfill({ json: farms });
    }

    if (path === "/farms" && method === "POST") {
      const body = request.postDataJSON();
      const farm = {
        id: farms.length + 1,
        name: body.name,
        location: body.location,
      };
      farms.push(farm);

      return route.fulfill({
        status: 201,
        json: {
          farmId: farm.id,
          message: "Farm was successfully created.",
        },
      });
    }

    const farmMatch = path.match(/^\/farms\/(\d+)$/);
    if (farmMatch && method === "GET") {
      const farm = farms.find((farm) => farm.id === Number(farmMatch[1]));

      return route.fulfill({
        status: farm ? 200 : 404,
        json: farm || { status: 404, message: "Farm not found.", errors: null },
      });
    }

    if (farmMatch && method === "PUT") {
      const farm = farms.find((farm) => farm.id === Number(farmMatch[1]));
      const body = request.postDataJSON();
      farm.name = body.name;
      farm.location = body.location;

      return route.fulfill({
        json: {
          farmId: farm.id,
          message: "Farm was successfully updated.",
        },
      });
    }

    const zonesMatch = path.match(/^\/farms\/(\d+)\/irrigation-zones$/);
    if (zonesMatch && method === "GET") {
      const farmId = Number(zonesMatch[1]);
      return route.fulfill({
        json: irrigationZones.filter((zone) => zone.farmId === farmId),
      });
    }

    if (zonesMatch && method === "POST") {
      const farmId = Number(zonesMatch[1]);
      const body = request.postDataJSON();
      const zone = {
        id: irrigationZones.length + 1,
        farmId,
        name: body.name,
        cropType: body.cropType,
        areaSize: body.areaSize,
        moistureThreshold: body.moistureThreshold,
      };
      irrigationZones.push(zone);

      return route.fulfill({
        status: 201,
        json: {
          irrigationZoneId: zone.id,
          message: "Irrigation zone was successfully created.",
        },
      });
    }

    return route.fulfill({
      status: 404,
      json: { status: 404, message: "Not found.", errors: null },
    });
  });

  await page.goto("/register");

  await page.getByPlaceholder("e.g. Farmer One").fill("E2E Farmer");
  await page.getByPlaceholder("e.g. farmer@example.com").fill(email);
  await page.getByPlaceholder("8-50 characters").fill("Password1");
  await page.getByRole("button", { name: "Create Account" }).click();

  await expect(page.getByRole("heading", { name: "My Farms" })).toBeVisible();
  await expect(page.getByText("E2E Farmer can only see farms created by this account.")).toBeVisible();

  await page.getByRole("button", { name: "+ Create New Farm" }).click();
  await page.getByPlaceholder("e.g. Golden Harvest").fill(farmName);
  await page.getByPlaceholder("City, State").fill("Eindhoven");
  await page.getByRole("button", { name: "Add Farm" }).click();

  const farmCard = page.locator(".farm-card").filter({ hasText: farmName });
  await expect(farmCard).toBeVisible();
  await expect(farmCard).toContainText("Eindhoven");
  await expect(page.getByText("Delete actions are available only for admins.")).toBeVisible();

  await farmCard.getByRole("button", { name: "View Details" }).click();

  await expect(page.getByRole("heading", { name: farmName })).toBeVisible();
  await expect(page.getByText("Delete actions are available only for admins.")).toBeVisible();

  await page.getByRole("button", { name: "+ Add Zone" }).click();
  await page.getByPlaceholder("e.g. Zone A").fill("Greenhouse A");
  await page.getByPlaceholder("e.g. Tomatoes").fill("Tomatoes");
  await page.getByPlaceholder("e.g. 12.5").fill("12.5");
  await page.getByPlaceholder("e.g. 35").fill("35");
  await page.getByRole("button", { name: "Create Zone" }).click();

  await expect(page.getByText("Irrigation zone was successfully created.")).toBeVisible();
  const zoneCard = page.locator(".zone-card").filter({ hasText: "Greenhouse A" });
  await expect(zoneCard).toBeVisible();
  await expect(zoneCard).toContainText("Tomatoes");
  await expect(zoneCard).toContainText("12.5 acres");
  await expect(zoneCard).toContainText("35% threshold");

  await page.getByRole("button", { name: "Edit" }).click();
  const editInputs = page.locator(".form-section .farm-form input");
  await editInputs.nth(0).fill(updatedFarmName);
  await editInputs.nth(1).fill("Tilburg");
  await page.getByRole("button", { name: "Save Changes" }).click();

  await expect(page.getByText("Farm was successfully updated.")).toBeVisible();
  await expect(page.getByRole("heading", { name: updatedFarmName })).toBeVisible();
  await expect(page.locator(".farm-location", { hasText: "Tilburg" })).toBeVisible();
});
