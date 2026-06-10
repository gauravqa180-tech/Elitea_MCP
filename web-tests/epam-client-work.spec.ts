import { test, expect } from '@playwright/test';

test('EPAM services Client Work page is reachable from header navigation', async ({ page }) => {
  await page.goto('https://www.epam.com/');

  await page.getByRole('button', { name: /Expand: Services/i }).click();
  await page.getByRole('link', { name: /Explore Our Client Work/i }).first().click();

  await expect(page.getByText('Client Work', { exact: true })).toBeVisible();
});
