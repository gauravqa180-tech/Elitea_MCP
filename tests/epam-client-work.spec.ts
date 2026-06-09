import { test, expect } from '@playwright/test';

test.describe('EPAM website navigation', () => {
  test('should open client work from services and verify Client Work text', async ({ page }) => {
    await page.goto('https://www.epam.com/');
    await page.getByRole('link', { name: 'Services' }).nth(1).click();
    await page.getByRole('link', { name: 'view all case studies' }).click();
    await expect(page.getByText('Client Work')).toBeVisible();
  });
});
