package eu.vvoleman.gympal.common.data.factory.sql

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import eu.vvoleman.gympal.database.GymPalDatabase

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class DriverFactory(
    private val context: android.content.Context
) {
    actual fun createDriver(): SqlDriver {
        return AndroidSqliteDriver(GymPalDatabase.Schema, context, "gympal.db")
    }
}