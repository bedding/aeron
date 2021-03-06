/*
 * Copyright 2014 - 2017 Real Logic Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#ifndef AERON_AERON_DRIVER_SENDER_PROXY_H
#define AERON_AERON_DRIVER_SENDER_PROXY_H

#include "aeron_driver_context.h"
#include "media/aeron_send_channel_endpoint.h"

typedef struct aeron_driver_sender_stct aeron_driver_sender_t;

typedef struct aeron_driver_sender_proxy_stct
{
    aeron_driver_sender_t *sender;
    aeron_threading_mode_t threading_mode;
    aeron_spsc_concurrent_array_queue_t *command_queue;
    int64_t *fail_counter;
}
aeron_driver_sender_proxy_t;

void aeron_driver_sender_proxy_add_endpoint(
    aeron_driver_sender_proxy_t *sender_proxy, aeron_send_channel_endpoint_t *endpoint);

void aeron_driver_sender_proxy_remove_endpoint(
    aeron_driver_sender_proxy_t *sender_proxy, aeron_send_channel_endpoint_t *endpoint);

void aeron_driver_sender_proxy_add_publication(
    aeron_driver_sender_proxy_t *sender_proxy, aeron_network_publication_t *publication);

void aeron_driver_sender_proxy_remove_publication(
    aeron_driver_sender_proxy_t *sender_proxy, aeron_network_publication_t *publication);

#endif //AERON_AERON_DRIVER_SENDER_PROXY_H
