package com.sean8.jeminai.core.testing.util

import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.Serializer
import kotlinx.coroutines.CoroutineScope
import org.junit.rules.TemporaryFolder

fun <T> TemporaryFolder.testDataStore(
    coroutineScope: CoroutineScope,
    serializer: Serializer<T>,
) = DataStoreFactory.create(
    serializer = serializer,
    scope = coroutineScope,
) {
    newFile()
}