package eu.vvoleman.gympal.common.data.factory.sql

import app.cash.sqldelight.db.SqlDriver
import eu.vvoleman.gympal.database.GymPalDatabase

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect class DriverFactory {
    fun createDriver(): SqlDriver
}

class DatabaseFactory(
    private val driverFactory: DriverFactory,
) {

    fun createDatabase(driver: SqlDriver? = null): GymPalDatabase {
        return GymPalDatabase(driver ?: driverFactory.createDriver())
    }
}