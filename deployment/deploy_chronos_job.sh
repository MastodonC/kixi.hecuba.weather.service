#!/bin/bash
curl -L -H 'Content-Type: application/json' -d @chronos_scheduler.json -X POST http://${master_internal_lb}/service/chronos/scheduler/iso8601
