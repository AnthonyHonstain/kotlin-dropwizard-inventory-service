package honstain

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.filter.Filter
import ch.qos.logback.core.spi.FilterReply
import com.fasterxml.jackson.annotation.JsonTypeName
import io.dropwizard.logging.filter.FilterFactory

/*
Got this example from
https://stackoverflow.com/questions/63773714/how-to-log-logs-with-different-log-levels-to-different-files-in-dropwizard

Also had to reference this since I am still getting used to Kotlin
https://kotlinlang.org/docs/interfaces.html#properties-in-interfaces
 */
@JsonTypeName("less-than-errors")
class LessThanErrorFilterFactory : FilterFactory<ILoggingEvent> {
    override fun build(): Filter<ILoggingEvent> {
        return object : Filter<ILoggingEvent>() {
            override fun decide(event: ILoggingEvent): FilterReply {
                return if (event.level != Level.ERROR) FilterReply.ACCEPT else FilterReply.DENY
            }
        }
    }
}