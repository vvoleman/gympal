package eu.vvoleman.gympal.common.data.factory.sql

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import eu.vvoleman.gympal.database.GymPalDatabase
import java.util.Properties

actual class DriverFactory {
    actual fun createDriver(): SqlDriver {
        val driver: SqlDriver = JdbcSqliteDriver("jdbc:sqlite:test.db", Properties(), GymPalDatabase.Schema)
        return driver
    }
}