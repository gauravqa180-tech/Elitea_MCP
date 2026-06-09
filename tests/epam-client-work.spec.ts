import { test, expect } from '@playwright/test';

test.describe('EPAM website navigation', () => {
  test('should open client work from services and verify Client Work text', async ({ page }) => {
    await page.goto('https://www.epam.com/');
  });
});
