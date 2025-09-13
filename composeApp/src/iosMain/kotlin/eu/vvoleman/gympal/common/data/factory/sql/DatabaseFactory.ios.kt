package eu.vvoleman.gympal.common.data.factory.sql

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import eu.vvoleman.gympal.database.GymPalDatabase

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class DriverFactory {
    actual fun createDriver(): SqlDriver {
        return NativeSqliteDriver(GymPalDatabase.Schema, "gympal.db")
    }
}
